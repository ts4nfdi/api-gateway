package org.semantics.apigateway.service;

import org.semantics.apigateway.collections.models.CollectionResource;
import org.semantics.apigateway.collections.models.TerminologyCollection;
import org.semantics.apigateway.config.DatabaseConfig;
import org.semantics.apigateway.model.CommonRequestParams;
import org.semantics.apigateway.model.TargetDbSchema;
import org.semantics.apigateway.model.responses.*;
import org.semantics.apigateway.service.configuration.ConfigurationLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
public abstract class AbstractEndpointService {

    protected final ConfigurationLoader configurationLoader;
    private final ResponseTransformerService responseTransformerService;
    private final CacheManager cacheManager;
    private final JsonLdTransform jsonLdTransform;
    protected final ResponseAggregatorService aggregatorTransformer;
    private final List<DatabaseConfig> ontologyConfigs;

    protected static final Logger logger = LoggerFactory.getLogger(AbstractEndpointService.class);

    public AbstractEndpointService(ConfigurationLoader configurationLoader, CacheManager cacheManager, JsonLdTransform jsonLdTransform, ResponseTransformerService responseTransformerService, Class<? extends AggregatedResourceBody> clazz) {
        this.configurationLoader = configurationLoader;
        this.ontologyConfigs = configurationLoader.getDatabaseConfigs();
        this.jsonLdTransform = jsonLdTransform;
        this.responseTransformerService = responseTransformerService;
        this.cacheManager = cacheManager;
        this.aggregatorTransformer = new ResponseAggregatorService(clazz);
    }

    public ApiAccessor getAccessor() {
        return new ApiAccessor(this.cacheManager);
    }

    protected AggregatedApiResponse transformForTargetDbSchema(AggregatedApiResponse data, TargetDbSchema targetDbSchemaEnum, String endpoint) {
        return transformForTargetDbSchema(data, targetDbSchemaEnum, endpoint, true);
    }

    protected AggregatedApiResponse transformForTargetDbSchema(AggregatedApiResponse data, TargetDbSchema targetDbSchemaEnum, String endpoint, Boolean isList) {
        String targetDbSchema = targetDbSchemaEnum == null ? "" : targetDbSchemaEnum.toString();

        if (targetDbSchema != null && !targetDbSchema.isEmpty()) {
            try {
                List<Map<String, Object>> collections;
                if (data instanceof AggregatedApiResponse) {
                    collections = data.getCollection();
                } else {
                    collections = (List<Map<String, Object>>) data;
                }
                Map<String, Object> transformedResults = responseTransformerService.transformAndStructureResults(collections, targetDbSchema, endpoint, isList);
                logger.debug("Transformed results for database schema: {}", transformedResults);
                AggregatedApiResponse transformedResponse = new AggregatedApiResponse();
                transformedResponse.setCollection(Collections.singletonList(transformedResults));
                transformedResponse.setList(false);
                return transformedResponse;
            } catch (IOException e) {
                throw new RuntimeException("Error transforming results for target database schema", e);
            }
        } else {
            return data;
        }
    }


    protected Map<String, UrlConfig> buildUrls(String database, String endpoint) {
        String[] databases = (database == null || database.isEmpty()) ? new String[0] : database.split(",");
        Stream<DatabaseConfig> configStream;

        if (databases.length == 0) {
            configStream = ontologyConfigs.stream();
        } else {
            configStream = ontologyConfigs.stream()
                    .filter(config -> Arrays.stream(databases)
                            .map(String::toLowerCase)
                            .anyMatch(f -> f.equals(config.getName()) || f.equals(config.getType())));
        }

        Map<String, UrlConfig> apiUrls = configStream.collect(
                Collectors.toMap(
                        config -> config.getUrl(endpoint),
                        config -> config.getUrlConfig(endpoint)
                )
        );

        if (apiUrls.isEmpty()) {
            String possible = ontologyConfigs.stream()
                    .map(DatabaseConfig::getName)
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException("Database not found: " + database + ". Possible values: " + possible);
        }

        return apiUrls;
    }

    protected AggregatedApiResponse filterPropertiesToDisplay(AggregatedApiResponse
                                                                      transformedResponse, CommonRequestParams commonRequestParams) {
        List<String> displayFields = commonRequestParams.getDisplay();
        if (displayFields == null || displayFields.isEmpty()) {
            return transformedResponse;
        }

        List<Map<String, Object>> collection = transformedResponse.getCollection();
        collection = collection.stream()
                .map(item -> {
                    Map<String, Object> newItem = new HashMap<>();
                    displayFields.forEach(field -> {
                        if (item.containsKey(field)) {
                            newItem.put(field, item.get(field));
                        }
                    });
                    return newItem;
                })
                .collect(Collectors.toList());

        transformedResponse.setCollection(collection);
        return transformedResponse;
    }

    protected AggregatedApiResponse transformJsonLd(AggregatedApiResponse transformedResponse, CommonRequestParams
            commonRequestParams) {
        String type = "";
        Map<String, String> context = new HashMap<>();
        try {
            Class<? extends AggregatedResourceBody> clazz = this.aggregatorTransformer.getClazz();
            type = jsonLdTransform.getTypeURI(clazz);
            context = jsonLdTransform.generateContext(clazz, commonRequestParams.getDisplay());
        } catch (Exception e) {
            logger.error("Error transforming json Ld", e);
        }
        transformedResponse = filterPropertiesToDisplay(transformedResponse, commonRequestParams);
        transformedResponse.setCollection(jsonLdTransform.convertToJsonLd(transformedResponse.getCollection(), type, context));
        return transformedResponse;
    }

    protected List<TransformedApiResponse> transformApiResponses(Map<String, ApiResponse> apiData, String
            endpoint) {
        return transformApiResponses(apiData, endpoint, false);
    }

    protected List<TransformedApiResponse> transformApiResponses(Map<String, ApiResponse> apiData, String
            endpoint, boolean paginate) {
        return apiData.entrySet().stream()
                .map(x -> this.transformSingleApiResponse(x, endpoint, paginate))
                .collect(Collectors.toList());
    }

    protected TransformedApiResponse transformSingleApiResponse(Map.Entry<String, ApiResponse> entry, String
            endpoint, boolean paginate) {
        String url = entry.getKey();
        ApiResponse results = entry.getValue();
        DatabaseConfig config = null;
        try {
            URL baseUrl = new URL(url);
            String baseUrlString = baseUrl.getProtocol() + "://" + baseUrl.getHost();
            config = this.configurationLoader.getConfigByBaseUrl(baseUrlString);
        } catch (Exception e) {
            logger.error("Error getting config for URL: {}", url, e);
        }

        return aggregatorTransformer.transformResponse(results, config, endpoint, paginate);
    }

    protected AggregatedApiResponse singleResponse(TransformedApiResponse transformedResponse, CommonRequestParams
            commonRequestParams) {
        if (transformedResponse == null) {
            return new AggregatedApiResponse();
        }

        AggregatedApiResponse aggregatedApiResponse = flattenResponseList(transformedResponse, commonRequestParams);
        aggregatedApiResponse.setList(false);

        return aggregatedApiResponse;
    }

    protected AggregatedApiResponse flattenResponseList(TransformedApiResponse data, CommonRequestParams
            commonRequestParams) {
        return flattenResponseList(List.of(data), commonRequestParams, null);
    }


    protected AggregatedApiResponse flattenResponseList(List<TransformedApiResponse> data,
                                                        CommonRequestParams commonRequestParams,
                                                        TerminologyCollection terminologyCollection) {
        AggregatedApiResponse aggregatedApiResponse = new AggregatedApiResponse();
        boolean showResponseConfiguration = commonRequestParams.isShowResponseConfiguration();
        boolean displayEmptyValues = commonRequestParams.isDisplayEmptyValues();
        aggregatedApiResponse.setShowConfig(showResponseConfiguration);
        aggregatedApiResponse.setTerminologyCollection(terminologyCollection);

        List<Map<String, Object>> aggregatedCollections = data.stream()
                .map(x -> x.getCollection(showResponseConfiguration, displayEmptyValues))
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
                .toList();

        aggregatedApiResponse.setCollection(aggregatedCollections);

        aggregatedApiResponse.setOriginalResponses(
                data.stream().map(TransformedApiResponse::getOriginalResponse)
                        .toList()
        );

        return aggregatedApiResponse;
    }

    protected AggregatedApiResponse filterOutByTerminologies
            (List<CollectionResource> terminologies, AggregatedApiResponse data) {
        if (terminologies == null || terminologies.isEmpty() || data == null || data.getCollection() == null) {
            return data;
        }

        List<Map<String, Object>> collection = data.getCollection();
        collection = collection.stream()
                .filter(map -> terminologies.stream().anyMatch(terminology ->
                        matchesTerminology(map, terminology)))
                .toList();
        data.setCollection(collection);

        return data;
    }

    private boolean matchesTerminology(Map<String, Object> map, CollectionResource terminology) {
        String sourceBaseUrl = (String) map.get("source_name");
        if (!terminology.getSource().equals(sourceBaseUrl)) {
            return false;
        }

        String ontologyIri = (String) map.get("ontology_iri");
        String iri = (String) map.get("iri");
        String ontology = (String) map.get("ontology");
        String shortForm = (String) map.get("short_form");


        String terminologyUri = terminology.getUri();
        if (terminologyUri != null && terminologyUri.equals(ontologyIri)) {
            return true;
        }

        if (terminologyUri != null && terminologyUri.equals(iri)) {
            return true;
        }

        String terminologyLabel = terminology.getLabel();
        if (terminologyLabel != null && terminologyLabel.equalsIgnoreCase(ontology)) {
            return true;
        }

        return shortForm != null && shortForm.equalsIgnoreCase(terminology.getLabel());
    }

    protected AggregatedApiResponse filterOutByCollection(TerminologyCollection
                                                                  terminologiesCollection, AggregatedApiResponse data) {
        if (terminologiesCollection == null) {
            return data;
        }

        return filterOutByTerminologies(terminologiesCollection.getTerminologies(), data);
    }

    protected AggregatedApiResponse paginate(TransformedApiResponse response, CommonRequestParams
            commonRequestParams, int page) {
        AggregatedApiResponse aggregatedApiResponse = new AggregatedApiResponse();
        aggregatedApiResponse.setPaginate(true);

        if (response == null) {
            return aggregatedApiResponse;
        }

        if (response.getPage() == 0) {
            enforcePagination(response, page);
        }

        boolean showResponseConfiguration = commonRequestParams.isShowResponseConfiguration();
        boolean displayEmpty = commonRequestParams.isDisplayEmptyValues();

        aggregatedApiResponse.setTotalCount(response.getTotalCollections());
        aggregatedApiResponse.setPage(response.getPage());
        aggregatedApiResponse.setCollection(response.getCollection(showResponseConfiguration, displayEmpty));
        aggregatedApiResponse.setShowConfig(showResponseConfiguration);
        aggregatedApiResponse.setOriginalResponses(List.of(response.getOriginalResponse()));

        return aggregatedApiResponse;
    }

    protected AggregatedApiResponse listResponse(TransformedApiResponse response, CommonRequestParams
            commonRequestParams) {
        AggregatedApiResponse aggregatedApiResponse = new AggregatedApiResponse();
        aggregatedApiResponse.setPaginate(false);

        if (response == null) {
            return aggregatedApiResponse;
        }

        boolean showResponseConfiguration = commonRequestParams.isShowResponseConfiguration();
        boolean displayEmpty = commonRequestParams.isDisplayEmptyValues();

        aggregatedApiResponse.setCollection(response.getCollection(showResponseConfiguration, displayEmpty));
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
        Map<String, UrlConfig> apiUrls = buildUrls(database, endpoint);


        if (accessor == null) {
            accessor = getAccessor();
        }

        accessor.setUrls(apiUrls);
        accessor.setLogger(logger);
        return accessor;
    }

    protected ApiAccessor applyCollection(ApiAccessor accessor, TerminologyCollection collection, String endpoint) {
        if (collection == null) {
            return accessor;
        }
        Map<String, UrlConfig> urls = new HashMap<>();
        Map<String, List<CollectionResource>> sources = collection.getTerminologies().
                stream().collect(Collectors.groupingBy(CollectionResource::getSource));

        sources.forEach((source, resources) -> {
            DatabaseConfig config = this.configurationLoader.getConfigByName(source);
            String collectionKey = config.getResponseMapping(endpoint).getCollectionFilter();
            String url = config.getUrl(endpoint);
            String terminologies = resources.stream()
                    .map(CollectionResource::getLabel)
                    .collect(Collectors.joining(","));

            if (url.contains("?") && collectionKey != null) {
                url = url + "&" + collectionKey + "=" + terminologies;
            } else if (collectionKey != null) {
                url = url + "?" + collectionKey + "=" + terminologies;
            }

            urls.put(url, config.getUrlConfig(endpoint));
        });
        accessor.setUrls(urls);
        return accessor;
    }


    public TransformedApiResponse selectResultsByDatabase(List<TransformedApiResponse> apiResponse, String
            database) {
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


    protected Object paginatedList(String acronym, String uri, String endpoint, CommonRequestParams params,
                                   Integer page, ApiAccessor accessor) {

        String database = params.getDatabase();
        TargetDbSchema targetDbSchema = params.getTargetDbSchema();
        accessor = initAccessor(database, endpoint, accessor);
        List<String> ids = getRequestIds(accessor, acronym, uri);
        ids.add(page.toString());

        return accessor.get(ids.toArray(new String[0]))
                .thenApply(data -> this.transformApiResponses(data, endpoint, true))
                .thenApply(data -> selectResultsByDatabase(data, database))
                .thenApply(x -> paginate(x, params, page))
                .thenApply(x -> transformJsonLd(x, params))
                .thenApply(data -> transformForTargetDbSchema(data, targetDbSchema, endpoint, true));
    }

    protected Object paginatedList(String id, String endpoint, CommonRequestParams params, Integer
            page, ApiAccessor accessor) {
        return paginatedList(id, null, endpoint, params, page, accessor);
    }


    protected CompletableFuture<AggregatedApiResponse> findAll(String acronym, String uri, String endpoint, CommonRequestParams params, ApiAccessor accessor) {
        String database = params.getDatabase();
        accessor = initAccessor(database, endpoint, accessor);
        List<String> ids = getRequestIds(accessor, acronym, uri);

        return accessor.get(ids.toArray(new String[0]))
                .thenApply(data -> this.transformApiResponses(data, endpoint))
                .thenApply(data -> selectResultsByDatabase(data, database))
                .thenApply(data -> listResponse(data, params))
                .thenApply(x -> transformJsonLd(x, params));
    }

    protected CompletableFuture<AggregatedApiResponse> findAll(String id, String endpoint, CommonRequestParams params, ApiAccessor
            accessor) {
        return findAll(id, null, endpoint, params, accessor);
    }

    private List<String> getRequestIds(ApiAccessor accessor, String acronym, String uri) {
        List<String> ids = new ArrayList<>(List.of(acronym));
        if (uri != null && !uri.isEmpty()) {
            uri = URLDecoder.decode(uri, StandardCharsets.UTF_8);
            String encodedUrl = URLEncoder.encode(uri, StandardCharsets.UTF_8);
            ids.add(encodedUrl);
            accessor.setUnDecodeUrl(true);
        }
        return ids;
    }

    protected AggregatedApiResponse findUri(String id, String uri, String endpoint, CommonRequestParams params, ApiAccessor
            accessor) {
        String database = params.getDatabase();
        TargetDbSchema targetDbSchema = params.getTargetDbSchema();
        accessor = initAccessor(database, endpoint, accessor);
        List<String> ids = getRequestIds(accessor, id, uri);
        try {
            return accessor.get(ids.toArray(new String[0]))
                    .thenApply(data -> this.transformApiResponses(data, endpoint))
                    .thenApply(x -> filterById(x, ids))
                    .thenApply(data -> selectResultsByDatabase(data, database))
                    .thenApply(x -> singleResponse(x, params))
                    .thenApply(x -> transformJsonLd(x, params))
                    .thenApply(data -> transformForTargetDbSchema(data, targetDbSchema, endpoint, false))
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    private List<TransformedApiResponse> filterById
            (List<TransformedApiResponse> apiResponses, List<String> ids) {
        if (ids == null || ids.size() > 1) {
            return apiResponses;
        }

        String id = ids.get(0);

        return apiResponses.stream().map(x -> {
                    List<AggregatedResourceBody> filtered = x.getCollection().stream().filter(y -> y.getShortForm().equalsIgnoreCase(id) || y.getIri().equals(id)).toList();
                    x.setCollection(filtered);
                    return x;
                })
                .filter(x -> !x.getCollection().isEmpty())
                .toList();
    }

}