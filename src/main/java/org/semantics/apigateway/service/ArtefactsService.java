package org.semantics.apigateway.service;

import org.semantics.apigateway.model.ResponseFormat;
import org.semantics.apigateway.model.TargetDbSchema;
import org.semantics.apigateway.model.responses.AggregatedApiResponse;
import org.semantics.apigateway.model.user.TerminologyCollection;
import org.semantics.apigateway.model.user.User;
import org.semantics.apigateway.service.auth.CollectionService;
import org.semantics.apigateway.service.configuration.ConfigurationLoader;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


@Service
public class ArtefactsService extends AbstractEndpointService {

    private final CollectionService collectionService;

    public ArtefactsService(ConfigurationLoader configurationLoader, CacheManager cacheManager, JsonLdTransform transform, ResponseTransformerService responseTransformerService, CollectionService collectionService) {
        super(configurationLoader, cacheManager, transform, responseTransformerService);
        this.collectionService = collectionService;
    }


    public CompletableFuture<Object> getArtefacts(String database, ResponseFormat format,
                                                  TargetDbSchema targetDbSchema,
                                                  boolean showResponseConfiguration,
                                                  String collectionId,
                                                  User currentUser) {
        return getArtefacts(database, format, targetDbSchema, showResponseConfiguration, collectionId, currentUser, null);
    }

    public CompletableFuture<Object> getArtefacts(String database, ResponseFormat format,
                                                  TargetDbSchema targetDbSchema,
                                                  boolean showResponseConfiguration,
                                                  String collectionId,
                                                  User currentUser,
                                                  ApiAccessor accessor) {

        CompletableFuture<Object> future = new CompletableFuture<>();
        Map<String, String> apiUrls;

        try {
            apiUrls = filterDatabases(database, "resources");
        } catch (Exception e) {
            future.completeExceptionally(e);
            return future;
        }


        if (accessor == null) {
            accessor = getAccessor();
        }

        accessor.setUrls(apiUrls);
        accessor.setLogger(logger);


        TerminologyCollection collection = collectionService.getCurrentUserCollection(collectionId, currentUser);

        return accessor.get()
                .thenApply(data -> this.transformApiResponses(data, "resources"))
                .thenApply(transformedData -> flattenResponseList(transformedData, showResponseConfiguration, collection))
                .thenApply(data -> filterOutByCollection(collection, data))
                .thenApply(data -> transformJsonLd(data, format))
                .thenApply(data -> transformForTargetDbSchema(data, targetDbSchema));
    }

    public CompletableFuture<Object> getArtefact(String id, ResponseFormat format, TargetDbSchema targetDbSchema, boolean showResponseConfiguration) {
        return getArtefact(id, format, targetDbSchema, showResponseConfiguration, null);
    }
    public CompletableFuture<Object> getArtefact(String id, ResponseFormat format, TargetDbSchema targetDbSchema, boolean showResponseConfiguration, ApiAccessor accessor) {

        Map<String, String> apiUrls = filterDatabases("", "resource_details");

        if (accessor == null) {
            accessor = getAccessor();
        }
        accessor.setUrls(apiUrls);
        accessor.setLogger(logger);
        accessor.setUnDecodeUrl(true);

        return accessor.get(id.toUpperCase())
                .thenApply(data -> this.transformApiResponses(data, "resource_details"))
                .thenApply(transformedData -> flattenResponseList(transformedData, showResponseConfiguration))
                .thenApply(data -> filterArtefactsById(data, id))
                .thenApply(data -> transformJsonLd(data, format))
                .thenApply(data -> transformForTargetDbSchema(data, targetDbSchema));
    }


    public CompletableFuture<Object> getArtefactTerm(String id, String uri, String database, ResponseFormat format, TargetDbSchema targetDbSchema, boolean showResponseConfiguration) {
        return getArtefactTerm(id, uri, database, format, targetDbSchema, showResponseConfiguration, null);
    }
    public CompletableFuture<Object> getArtefactTerm(String id, String uri, String database, ResponseFormat format, TargetDbSchema targetDbSchema, boolean showResponseConfiguration, ApiAccessor accessor) {
        Map<String, String> apiUrls = filterDatabases(database, "concept_details");

        String encodedUrl = URLEncoder.encode(uri, StandardCharsets.UTF_8);

        if (accessor == null) {
            accessor = getAccessor();
        }
        accessor.setUrls(apiUrls);
        accessor.setLogger(logger);
        accessor.setUnDecodeUrl(true);

        return accessor.get(id.toUpperCase(), encodedUrl)
                .thenApply(data -> this.transformApiResponses(data, "concept_details"))
                .thenApply(transformedData -> flattenResponseList(transformedData, showResponseConfiguration))
                .thenApply(data -> transformJsonLd(data, format))
                .thenApply(data -> transformForTargetDbSchema(data, targetDbSchema));
    }


    private AggregatedApiResponse filterArtefactsById(AggregatedApiResponse transformedResponse, String id) {
        List<Map<String, Object>> filtredList = transformedResponse.getCollection().stream().filter(x -> x.getOrDefault("label", "").toString().equalsIgnoreCase(id)).collect(Collectors.toList());
        transformedResponse.setCollection(filtredList);
        return transformedResponse;
    }
}
