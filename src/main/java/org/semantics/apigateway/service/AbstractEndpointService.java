package org.semantics.apigateway.service;

import org.semantics.apigateway.config.DatabaseConfig;
import org.semantics.apigateway.model.ResponseFormat;
import org.semantics.apigateway.model.TargetDbSchema;
import org.semantics.apigateway.model.responses.AggregatedApiResponse;
import org.semantics.apigateway.model.responses.ApiResponse;
import org.semantics.apigateway.model.responses.TransformedApiResponse;
import org.semantics.apigateway.model.user.TerminologyCollection;
import org.semantics.apigateway.service.configuration.ConfigurationLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public abstract class AbstractEndpointService {

    private final ConfigurationLoader configurationLoader;
    private final ResponseTransformerService responseTransformerService;
    private final CacheManager cacheManager;
    private final JsonLdTransform jsonLdTransform;
    protected static final Logger logger = LoggerFactory.getLogger(AbstractEndpointService.class);


    private final ResponseAggregatorService dynTransformResponse = new ResponseAggregatorService();

    private final List<DatabaseConfig> ontologyConfigs;


    public AbstractEndpointService(ConfigurationLoader configurationLoader, CacheManager cacheManager, JsonLdTransform jsonLdTransform, ResponseTransformerService responseTransformerService) {
        this.configurationLoader = configurationLoader;
        this.ontologyConfigs = configurationLoader.getDatabaseConfigs();
        this.jsonLdTransform = jsonLdTransform;
        this.responseTransformerService = responseTransformerService;
        this.cacheManager = cacheManager;
    }

    public ApiAccessor getAccessor() {
        return new ApiAccessor(this.cacheManager);
    }

    protected Object transformForTargetDbSchema(Object data, TargetDbSchema targetDbSchemaEnum) {
        String targetDbSchema = targetDbSchemaEnum == null ? "" : targetDbSchemaEnum.toString();

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
        String[] databases = (database == null || database.isEmpty()) ? new String[0] : database.split(",");

        if (databases.length == 0) {
            apiUrls = ontologyConfigs.stream().collect(Collectors.toMap(dbConfig -> dbConfig.getUrl(endpoint), DatabaseConfig::getApiKey));
        } else {

            apiUrls = Arrays.stream(databases)
                    .flatMap(x -> ontologyConfigs.stream().filter(db -> db.getName().equals(x.toLowerCase()) || db.getType().equals(x.toLowerCase())))
                    .collect(Collectors.toMap(dbConfig -> dbConfig.getUrl(endpoint), DatabaseConfig::getApiKey));

            if (apiUrls.isEmpty()) {
                String possibleValues = ontologyConfigs.stream().map(DatabaseConfig::getName).collect(Collectors.joining(","));
                throw new IllegalArgumentException("Database not found: " + database + " . Possible values are: " + possibleValues);
            }
        }
        return apiUrls;
    }

    protected Object transformJsonLd(AggregatedApiResponse transformedResponse, ResponseFormat formatEnum) {
        String format = formatEnum == null ? "" : formatEnum.toString();

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
        DatabaseConfig config = null;
        try {
            config = this.configurationLoader.getConfigByUrl(url, endpoint);
        } catch (Exception e) {
            logger.error("Error getting config for URL: {}", url, e);
        }

        TransformedApiResponse transformedResponse = dynTransformResponse.dynTransformResponse(results, config, endpoint);

        logger.debug(
                "Transformed API {} Response: {}", url, transformedResponse.getCollection());
        return transformedResponse;
    }

    protected AggregatedApiResponse flattenResponseList(List<TransformedApiResponse> data, boolean showResponseConfiguration) {
        return flattenResponseList(data, showResponseConfiguration, null);
    }

    protected AggregatedApiResponse flattenResponseList(List<TransformedApiResponse> data, boolean showResponseConfiguration, TerminologyCollection terminologyCollection) {
        AggregatedApiResponse aggregatedApiResponse = new AggregatedApiResponse();

        aggregatedApiResponse.setShowConfig(showResponseConfiguration);
        aggregatedApiResponse.setTerminologyCollection(terminologyCollection);

        List<Map<String, Object>> aggregatedCollections = data.stream().map(x -> x.getCollection(showResponseConfiguration))
                .flatMap(List::stream)
                .sorted((m1, m2) -> {
                    String label1 = (String) m1.getOrDefault("label", "");
                    String label2 = (String) m2.getOrDefault("label", "");
                    if (label1 == null)
                        label1 = "";

                    if (label2 == null)
                        label2 = "";

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
