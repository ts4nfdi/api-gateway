package org.semantics.apigateway.service.artefacts;

import org.semantics.apigateway.model.CommonRequestParams;
import org.semantics.apigateway.model.responses.AggregatedApiResponse;
import org.semantics.apigateway.model.user.TerminologyCollection;
import org.semantics.apigateway.model.user.User;
import org.semantics.apigateway.service.AbstractEndpointService;
import org.semantics.apigateway.service.ApiAccessor;
import org.semantics.apigateway.service.JsonLdTransform;
import org.semantics.apigateway.service.ResponseTransformerService;
import org.semantics.apigateway.service.auth.CollectionService;
import org.semantics.apigateway.service.configuration.ConfigurationLoader;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


@Service
public class ArtefactsService extends AbstractEndpointService {

    private final CollectionService collectionService;

    public ArtefactsService(ConfigurationLoader configurationLoader, CacheManager cacheManager, JsonLdTransform transform, ResponseTransformerService responseTransformerService, CollectionService collectionService) {
        super(configurationLoader, cacheManager, transform, responseTransformerService);
        this.collectionService = collectionService;
    }


    public Object getArtefacts(CommonRequestParams params, String collectionId, User currentUser, ApiAccessor accessor) {
        String endpoint = "resources";
        try {
            return
                    findAllArtefacts(params, collectionId, currentUser, accessor)
                            .thenApply(data -> transformJsonLd(data, params.getFormat()))
                            .thenApply(data -> transformForTargetDbSchema(data, params.getTargetDbSchema(), endpoint)).get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }


    public Object getArtefact(String id, CommonRequestParams params, ApiAccessor accessor) {
        return findUri(id, null, "resource_details", params, accessor);
    }


    public Object searchMetadata(String query, CommonRequestParams params, ApiAccessor accessor) {
        String endpoint = "resources";
        return findAllArtefacts(params, null, null, accessor)
                .thenApply(data -> filterOutByQuery(query, data))
                .thenApply(data -> transformJsonLd(data, params.getFormat()))
                .thenApply(data -> transformForTargetDbSchema(data, params.getTargetDbSchema(), endpoint));
    }

    private AggregatedApiResponse filterOutByQuery(String query, AggregatedApiResponse data) {
        if (query == null || query.isEmpty()) {
            return data;
        }

        List<Map<String, Object>> filtered = data.getCollection().stream()
                .filter(item -> {
                    if(item == null) {
                        return false;
                    }

                    String label = item.get("label") == null ? "" : item.get("label").toString();
                    String iri = item.get("iri") == null ? "" : item.get("iri").toString();
                    boolean result = label.toLowerCase().contains(query.toLowerCase());
                    result = result || iri.toLowerCase().contains(query.toLowerCase());
                    return result;
                })
                .toList();

        data.setCollection(filtered);

        return data;
    }

    private CompletableFuture<AggregatedApiResponse> findAllArtefacts(CommonRequestParams params, String collectionId, User currentUser, ApiAccessor accessor) {
        String endpoint = "resources";
        String database = params.getDatabase();
        boolean showResponseConfiguration = params.isShowResponseConfiguration();

        accessor = initAccessor(database, endpoint, accessor);
        TerminologyCollection collection = collectionService.getCurrentUserCollection(collectionId, currentUser);

        return accessor.get()
                .thenApply(data -> this.transformApiResponses(data, endpoint))
                .thenApply(transformedData -> flattenResponseList(transformedData, showResponseConfiguration, collection))
                .thenApply(data -> filterOutByCollection(collection, data));
    }
}
