package org.semantics.apigateway.service.search;

import lombok.Getter;
import org.apache.lucene.queryparser.classic.ParseException;
import org.semantics.apigateway.config.OntologyConfig;
import org.semantics.apigateway.model.Database;
import org.semantics.apigateway.model.ResponseFormat;
import org.semantics.apigateway.model.TargetDbSchema;
import org.semantics.apigateway.model.responses.AggregatedApiResponse;
import org.semantics.apigateway.model.responses.ApiResponse;
import org.semantics.apigateway.model.responses.TransformedApiResponse;
import org.semantics.apigateway.service.ResponseAggregatorService;
import org.semantics.apigateway.service.ConfigurationLoader;
import org.semantics.apigateway.service.JsonLdTransform;
import org.semantics.apigateway.service.ResponseTransformerService;
import org.semantics.apigateway.service.ApiAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


@Service
public class SearchService {

    @Autowired
    private ConfigurationLoader configurationLoader;

    @Autowired
    private SearchLocalIndexerService localIndexer;

    @Autowired
    private ResponseTransformerService responseTransformerService;

    @Getter
    @Autowired
    private ApiAccessor accessor;

    @Autowired
    private JsonLdTransform jsonLdTransform;


    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);


    private final ResponseAggregatorService dynTransformResponse = new ResponseAggregatorService();

    private List<OntologyConfig> ontologyConfigs;


    public SearchService(ConfigurationLoader configurationLoader, SearchLocalIndexerService localIndexer) {
        this.ontologyConfigs = configurationLoader.getOntologyConfigs();
        this.localIndexer = localIndexer;
    }


    public CompletableFuture<Object> performSearch(
            String query, Database database, ResponseFormat format,
            TargetDbSchema targetDbSchema, boolean showResponseConfiguration) {

        String db = "", formatStr = "", target = "";

        if (database != null) {
            db = database.toString();
        }
        if (format != null) {
            formatStr = format.toString();
        }
        if (targetDbSchema != null) {
            target = targetDbSchema.toString();
        }

        return performSearch(query, db, formatStr, target, showResponseConfiguration);
    }

    // Performs a federated search across multiple databases and optionally transforms the results for a target database schema]
    public CompletableFuture<Object> performSearch(
            String query, String database, String format,
            String targetDbSchema, boolean showResponseConfiguration) {

        CompletableFuture<Object> future = new CompletableFuture<>();

        if (!this.configurationLoader.databaseExist(database)) {
            future.completeExceptionally(new IllegalArgumentException("Database not found: " + database));
            return future;
        }

        Map<String, String> apiUrls = ontologyConfigs.stream()
                .collect(Collectors.toMap(OntologyConfig::getUrl, OntologyConfig::getApiKey));

        accessor.setUrls(apiUrls);
        accessor.setLogger(logger);

        return accessor.get(query)
                .thenApply(originalData -> transformApiResponses(originalData, format))
                .thenApply(transformedData -> flattenResponseList(transformedData, showResponseConfiguration))
                .thenApply(data -> reIndexResults(query, data))
                .thenApply(data -> transformForTargetDbSchema(data, targetDbSchema));
    }


    private List<TransformedApiResponse> transformApiResponses(Map<String, ApiResponse> apiData, String format) {
        return apiData.entrySet().stream()
                .map(data -> transformSingleApiResponse(data, format))
                .collect(Collectors.toList());
    }

    private TransformedApiResponse transformSingleApiResponse(Map.Entry<String, ApiResponse> entry, String format) {
        String url = entry.getKey();
        ApiResponse results = entry.getValue();

        OntologyConfig config = this.configurationLoader.getConfigByUrl(url);

        TransformedApiResponse transformedResponse = dynTransformResponse.dynTransformResponse(results, config);

        if (jsonLdTransform.isJsonLdFormat(format)) {
            transformedResponse = jsonLdTransform.convertToJsonLd(transformedResponse, config);
        }


        logger.debug("Transformed API Response: {}", transformedResponse);
        return transformedResponse;
    }


    private AggregatedApiResponse flattenResponseList(List<TransformedApiResponse> data, boolean showResponseConfiguration) {

        AggregatedApiResponse aggregatedApiResponse = new AggregatedApiResponse();

        aggregatedApiResponse.setShowConfig(showResponseConfiguration);

        List<Map<String,Object>> aggregatedCollections = data.stream().map(TransformedApiResponse::getCollection)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        aggregatedApiResponse.setCollection(aggregatedCollections);

        aggregatedApiResponse.setOriginalResponses(
                data.stream().map(TransformedApiResponse::getOriginalResponse)
                        .collect(Collectors.toList())
        );

        return aggregatedApiResponse;
    }

    private AggregatedApiResponse reIndexResults(String query, AggregatedApiResponse data) {
        List<Map<String, Object>> collection = data.getCollection();
        try {
             collection = this.localIndexer.reIndexResults(query.replace("*", ""), collection, logger);
        } catch (IOException | ParseException e) {
            throw new RuntimeException("Error during re-indexing results", e);
        }
        data.setCollection(collection);
        return data;
    }

    private Object transformForTargetDbSchema(AggregatedApiResponse data, String targetDbSchema) {
        if (targetDbSchema != null && !targetDbSchema.isEmpty()) {
            try {
                Object transformedResults = responseTransformerService.transformAndStructureResults(data.getCollection(), targetDbSchema);
                logger.debug("Transformed results for database schema: {}", transformedResults);
                return transformedResults;
            } catch (IOException e) {
                throw new RuntimeException("Error transforming results for target database schema", e);
            }
        } else {
            return data;
        }
    }


}
