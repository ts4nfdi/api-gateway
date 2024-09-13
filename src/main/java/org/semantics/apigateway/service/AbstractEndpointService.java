package org.semantics.apigateway.service;

import lombok.Getter;
import org.semantics.apigateway.config.DatabaseConfig;
import org.semantics.apigateway.model.ResponseFormat;
import org.semantics.apigateway.model.TargetDbSchema;
import org.semantics.apigateway.model.responses.AggregatedApiResponse;
import org.semantics.apigateway.model.responses.ApiResponse;
import org.semantics.apigateway.model.responses.TransformedApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


@Service
public abstract class AbstractEndpointService {

    @Autowired
    private ConfigurationLoader configurationLoader;

    @Autowired
    private ResponseTransformerService responseTransformerService;

    @Getter
    @Autowired
    private ApiAccessor accessor;

    @Autowired
    private JsonLdTransform jsonLdTransform;


    protected static final Logger logger = LoggerFactory.getLogger(AbstractEndpointService.class);


    private final ResponseAggregatorService dynTransformResponse = new ResponseAggregatorService();

    private List<DatabaseConfig> ontologyConfigs;


    public AbstractEndpointService(ConfigurationLoader configurationLoader) {
        this.ontologyConfigs = configurationLoader.getDatabaseConfigs();
    }


    protected Object transformForTargetDbSchema(Object data, String targetDbSchema) {
        if (targetDbSchema != null && !targetDbSchema.isEmpty()) {
            try {
                List<Map<String, Object>> collections;
                if (data instanceof AggregatedApiResponse) {
                    collections = ((AggregatedApiResponse) data).getCollection();
                } else {
                    collections = (List<Map<String, Object>>) data;
                }
                Object transformedResults = responseTransformerService.transformAndStructureResults(collections, targetDbSchema);
                logger.debug("Transformed results for database schema: {}", transformedResults);
                return transformedResults;
            } catch (IOException e) {
                throw new RuntimeException("Error transforming results for target database schema", e);
            }
        } else {
            return data;
        }
    }

    protected Map<String, String> filterDatabases(String database, String endpoint) {
        Map<String, String> apiUrls;
        String[] databases = database.split(",");

        if (database.isEmpty()) {
            apiUrls = ontologyConfigs.stream().collect(Collectors.toMap(dbConfig -> dbConfig.getUrl(endpoint), DatabaseConfig::getApiKey));
        } else {

            apiUrls = Arrays.stream(databases)
                    .flatMap(x -> ontologyConfigs.stream().filter(db -> db.getName().equals(x.toLowerCase()) || db.getType().equals(x.toLowerCase())))
                    .collect(Collectors.toMap(DatabaseConfig::getArtefactsUrl, DatabaseConfig::getApiKey));

            if (apiUrls.isEmpty()) {
                String possibleValues = ontologyConfigs.stream().map(DatabaseConfig::getName).collect(Collectors.joining(","));
                throw new IllegalArgumentException("Database not found: " + database + " . Possible values are: " + possibleValues);
            }
        }
        return apiUrls;
    }

    protected Object transformJsonLd(AggregatedApiResponse transformedResponse, String format) {
        if (jsonLdTransform.isJsonLdFormat(format)) {
            return jsonLdTransform.convertToJsonLd(transformedResponse.getCollection());
        } else {
            return transformedResponse;
        }
    }

    protected List<TransformedApiResponse> transformApiResponses(Map<String, ApiResponse> apiData, String endpoint) {
        return apiData.entrySet().stream()
                .map(x -> this.transformSingleApiResponse(x, endpoint))
                .collect(Collectors.toList());
    }

    protected TransformedApiResponse transformSingleApiResponse(Map.Entry<String, ApiResponse> entry, String endpoint) {
        String url = entry.getKey();
        ApiResponse results = entry.getValue();

        DatabaseConfig config = this.configurationLoader.getConfigByUrl(url, endpoint);

        TransformedApiResponse transformedResponse = dynTransformResponse.dynTransformResponse(results, config, endpoint);

        logger.info("Transformed API Response: {}", transformedResponse);
        return transformedResponse;
    }

    protected AggregatedApiResponse flattenResponseList(List<TransformedApiResponse> data, boolean showResponseConfiguration) {
        AggregatedApiResponse aggregatedApiResponse = new AggregatedApiResponse();

        aggregatedApiResponse.setShowConfig(showResponseConfiguration);

        List<Map<String, Object>> aggregatedCollections = data.stream().map(TransformedApiResponse::getCollection)
                .flatMap(List::stream)
                .sorted((m1, m2) -> {
                    String label1 = (String) m1.getOrDefault("label", "");
                    String label2 = (String) m2.getOrDefault("label", "");
                    return label1.compareTo(label2);
                })
                .collect(Collectors.toList());

        aggregatedApiResponse.setCollection(aggregatedCollections);

        aggregatedApiResponse.setOriginalResponses(
                data.stream().map(TransformedApiResponse::getOriginalResponse)
                        .collect(Collectors.toList())
        );

        return aggregatedApiResponse;
    }
}
