package org.semantics.apigateway.service;

import org.semantics.apigateway.config.DatabaseConfig;
import org.semantics.apigateway.model.CommonRequestParams;
import org.semantics.apigateway.model.ResponseFormat;
import org.semantics.apigateway.model.TargetDbSchema;
import org.semantics.apigateway.model.responses.*;
import org.semantics.apigateway.model.user.TerminologyCollection;
import org.semantics.apigateway.service.configuration.ConfigurationLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
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

    protected Object transformForTargetDbSchema(Object data, TargetDbSchema targetDbSchemaEnum, String endpoint) {
        return transformForTargetDbSchema(data, targetDbSchemaEnum, endpoint, true);
    }

    protected Object transformForTargetDbSchema(Object data, TargetDbSchema targetDbSchemaEnum, String endpoint, Boolean isList) {
        String targetDbSchema = targetDbSchemaEnum == null ? "" : targetDbSchemaEnum.toString();

        if (targetDbSchema != null && !targetDbSchema.isEmpty()) {
            try {
                List<Map<String, Object>> collections;
                if (data instanceof AggregatedApiResponse) {
                    collections = ((AggregatedApiResponse) data).getCollection();
                } else {
                    collections = (List<Map<String, Object>>) data;
                }
                Object transformedResults = responseTransformerService.transformAndStructureResults(collections, targetDbSchema, endpoint, isList);
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
                //TODO: better supporting of error showing
                throw new IllegalArgumentException("Database not found: " + database + " . Possible values are: " + possibleValues);
            }
        }
        return apiUrls;
    }

    protected Object transformJsonLd(AggregatedApiResponse transformedResponse, ResponseFormat formatEnum) {
        String format = formatEnum == null ? "" : formatEnum.toString();

        if (jsonLdTransform.isJsonLdFormat(format)) {
            transformedResponse.setCollection(jsonLdTransform.convertToJsonLd(transformedResponse.getCollection()));
        }

        return transformedResponse;
    }

    protected List<TransformedApiResponse> transformApiResponses(Map<String, ApiResponse> apiData, String endpoint) {
        return transformApiResponses(apiData, endpoint, false);
    }

    protected List<TransformedApiResponse> transformApiResponses(Map<String, ApiResponse> apiData, String endpoint, boolean paginate) {
        return apiData.entrySet().stream()
                .map(x -> this.transformSingleApiResponse(x, endpoint, paginate))
                .collect(Collectors.toList());
    }

    protected TransformedApiResponse transformSingleApiResponse(Map.Entry<String, ApiResponse> entry, String endpoint, boolean paginate) {
        String url = entry.getKey();
        ApiResponse results = entry.getValue();
        DatabaseConfig config = null;
        try {
            config = this.configurationLoader.getConfigByUrl(url, endpoint);
        } catch (Exception e) {
            logger.error("Error getting config for URL: {}", url, e);
        }

        return dynTransformResponse.dynTransformResponse(results, config, endpoint, paginate);
    }

    protected AggregatedApiResponse singleResponse(TransformedApiResponse transformedResponse, boolean showResponseConfiguration) {
        if(transformedResponse == null){
            return new AggregatedApiResponse();
        }

        AggregatedApiResponse aggregatedApiResponse = flattenResponseList(transformedResponse, showResponseConfiguration);
        aggregatedApiResponse.setList(false);

        return aggregatedApiResponse;
    }

    protected AggregatedApiResponse flattenResponseList(TransformedApiResponse data, boolean showResponseConfiguration) {
        return flattenResponseList(List.of(data), showResponseConfiguration, null);
    }

    protected AggregatedApiResponse flattenResponseList(List<TransformedApiResponse> data, boolean showResponseConfiguration) {
        return flattenResponseList(data, showResponseConfiguration, null);
    }

    protected AggregatedApiResponse flattenResponseList(List<TransformedApiResponse> data,
                                                        boolean showResponseConfiguration,
                                                        TerminologyCollection terminologyCollection) {
        AggregatedApiResponse aggregatedApiResponse = new AggregatedApiResponse();

        aggregatedApiResponse.setShowConfig(showResponseConfiguration);
        aggregatedApiResponse.setTerminologyCollection(terminologyCollection);

        List<Map<String, Object>> aggregatedCollections = data.stream()
                .map(x -> x.getCollection(showResponseConfiguration))
                .flatMap(List::stream)
                .sorted((m1, m2) -> {
                    String label1 = (String) m1.getOrDefault("label", "");
                    String label2 = (String) m2.getOrDefault("label", "");
                    if (label1 == null) {
                        label1 = "";
                    }

                    if (label2 == null) {
                        label2 = "";
                    }

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

    protected AggregatedApiResponse filterOutByTerminologies(String[] terminologies, AggregatedApiResponse data) {
        if (terminologies == null || terminologies.length == 0) {
            return data;
        }

        List<Map<String, Object>> collection = data.getCollection();
        collection = collection.stream()
                .filter(map -> {
                    String terminology = (String) map.get("ontology");
                    return Arrays.stream(terminologies).map(String::toLowerCase).toList().contains(terminology.toLowerCase());
                })
                .collect(Collectors.toList());
        data.setCollection(collection);

        return data;
    }

    protected AggregatedApiResponse filterOutByCollection(TerminologyCollection terminologiesCollection, AggregatedApiResponse data) {
        if (terminologiesCollection == null) {
            return data;
        }

        return filterOutByTerminologies(terminologiesCollection.getTerminologies().toArray(new String[0]), data);
    }

    protected AggregatedApiResponse paginate(TransformedApiResponse response, boolean showResponseConfiguration, int page) {
        AggregatedApiResponse aggregatedApiResponse = new AggregatedApiResponse();
        aggregatedApiResponse.setPaginate(true);

        if (response == null) {
            return aggregatedApiResponse;
        }

        if(response.getPage() == 0){
            enforcePagination(response, page);
        }

        aggregatedApiResponse.setTotalCount(response.getTotalCollections());
        aggregatedApiResponse.setPage(response.getPage());
        aggregatedApiResponse.setCollection(response.getCollection(showResponseConfiguration));
        aggregatedApiResponse.setShowConfig(showResponseConfiguration);
        aggregatedApiResponse.setOriginalResponses(List.of(response.getOriginalResponse()));

        return aggregatedApiResponse;
    }

    private void enforcePagination(TransformedApiResponse response, int page) {
        int pageSize = PaginatedResponse.PAGE_SIZE;
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, response.getCollection().size());
        List<AggregatedResourceBody> collection = response.getCollection().subList(start, end);

        response.setPage(page);
        response.setTotalCollections(response.getCollection().size());
        response.setCollection(collection);
    }

    protected ApiAccessor initAccessor(String database, String endpoint, ApiAccessor accessor) {
        Map<String, String> apiUrls = filterDatabases(database, endpoint);


        if (accessor == null) {
            accessor = getAccessor();
        }

        accessor.setUrls(apiUrls);
        accessor.setLogger(logger);
        return accessor;
    }

    public TransformedApiResponse selectResultsByDatabase(List<TransformedApiResponse> apiResponse, String database) {
        TransformedApiResponse a = null;
        // TODO: update this to merge the results instead of returning only one the first one
        if (database != null) {
         a = apiResponse.stream()
                    .filter(x -> !x.getCollection().isEmpty() && x.getCollection().get(0).getBackendType().equals(database))
                    .findFirst()
                    .orElse(null);
        }


        if (a == null) {
            a = apiResponse.stream().filter(x -> !x.getCollection().isEmpty())
                    .findFirst().orElse(null);
        }

        return a;
    }

    protected Object paginatedList(String id, String endpoint, CommonRequestParams params, Integer page, ApiAccessor accessor) {
        String database = params.getDatabase();
        ResponseFormat format = params.getFormat();
        TargetDbSchema targetDbSchema = params.getTargetDbSchema();
        boolean showResponseConfiguration = params.isShowResponseConfiguration();

        accessor = initAccessor(database, endpoint, accessor);

        return accessor.get(id.toUpperCase(), page.toString())
                .thenApply(data -> this.transformApiResponses(data, endpoint, true))
                .thenApply(data -> selectResultsByDatabase(data, database))
                .thenApply(x -> paginate(x, showResponseConfiguration, page))
                .thenApply(data -> transformJsonLd(data, format))
                .thenApply(data -> transformForTargetDbSchema(data, targetDbSchema, endpoint, true));
    }

    protected Object findUri(String id, String uri, String endpoint, CommonRequestParams params, ApiAccessor accessor) {
        String database = params.getDatabase();
        ResponseFormat format = params.getFormat();
        TargetDbSchema targetDbSchema = params.getTargetDbSchema();
        boolean showResponseConfiguration = params.isShowResponseConfiguration();

        accessor = initAccessor(database, endpoint, accessor);

        id = id.equals("gnd") ? "gnd" : id.toUpperCase();
        List<String> ids = new ArrayList<>(List.of(id));

        if(uri != null && !uri.isEmpty()){
            String encodedUrl = URLEncoder.encode(uri, StandardCharsets.UTF_8);
            ids.add(encodedUrl);
            accessor.setUnDecodeUrl(true);
        }

        try {
            return accessor.get(ids.toArray(new String[0]))
                    .thenApply(data -> this.transformApiResponses(data, endpoint))
                    .thenApply(data -> selectResultsByDatabase(data, database))
                    .thenApply(x -> singleResponse(x, showResponseConfiguration))
                    .thenApply(data -> transformJsonLd(data, format))
                    .thenApply(data -> transformForTargetDbSchema(data, targetDbSchema, endpoint, false))
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            return null;
        }
    }


}
