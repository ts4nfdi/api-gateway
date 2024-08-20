package org.semantics.apigateway.service.search;

import lombok.Getter;
import org.apache.lucene.queryparser.classic.ParseException;
import org.semantics.apigateway.config.OntologyConfig;
import org.semantics.apigateway.service.ResponseAggregatorService;
import org.semantics.apigateway.service.ConfigurationLoader;
import org.semantics.apigateway.service.JsonLdTransform;
import org.semantics.apigateway.service.ResponseTransformerService;
import org.semantics.apigateway.service.ApiAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
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

    // Performs a federated search across multiple databases and optionally transforms the results for a target database schema]
    public CompletableFuture<Object> performSearch(
            String query, String database, String format, String targetDbSchema){

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
                .thenApply(data -> transformApiResponses(data, format))
                .thenApply(this::flattenResponseList)
                .thenApply(data -> reIndexResults(query, data))
                .thenApply(data -> transformForTargetDbSchema(data, targetDbSchema));
    }


    private List<List<Map<String, Object>>> transformApiResponses(Map<String, Map<String, Object>> apiData, String format) {
        return apiData.entrySet().stream()
                .map( data  -> transformSingleApiResponse(data, format))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> transformSingleApiResponse(Map.Entry<String, Map<String, Object>> entry, String format) {
        String url = entry.getKey();
        Map<String, Object> results = entry.getValue();

        OntologyConfig config = this.configurationLoader.getConfigByUrl(url);

        List<Map<String, Object>> transformedResponse = dynTransformResponse.dynTransformResponse(results, config);

        if (jsonLdTransform.isJsonLdFormat(format)) {
            transformedResponse = jsonLdTransform.convertToJsonLd(transformedResponse, config);
        }

        logger.debug("Transformed API Response: {}", transformedResponse);
        return transformedResponse;
    }


    private List<Map<String, Object>> flattenResponseList(List<List<Map<String, Object>>> data) {
        return data.stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> reIndexResults(String query, List<Map<String, Object>> data) {
        try {
            return this.localIndexer.reIndexResults(query.replace("*", ""), data, logger);
        } catch (IOException | ParseException e) {
            throw new RuntimeException("Error during re-indexing results", e);
        }
    }

    private Object transformForTargetDbSchema(List<Map<String, Object>> data, String targetDbSchema) {
        if (targetDbSchema != null && !targetDbSchema.isEmpty()) {
            try {
                Object transformedResults = responseTransformerService.transformAndStructureResults(data, targetDbSchema);
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
