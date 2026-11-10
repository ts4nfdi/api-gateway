package org.semantics.apigateway.artefacts.metadata;

import org.semantics.apigateway.collections.CollectionService;
import org.semantics.apigateway.collections.models.TerminologyCollection;
import org.semantics.apigateway.collections.models.TerminologyStatisticDto;
import org.semantics.apigateway.model.CommonRequestParams;
import org.semantics.apigateway.model.SemanticArtefact;
import org.semantics.apigateway.model.responses.AggregatedApiResponse;
import org.semantics.apigateway.model.user.User;
import org.semantics.apigateway.service.AbstractEndpointService;
import org.semantics.apigateway.service.ApiAccessor;
import org.semantics.apigateway.service.JsonLdTransform;
import org.semantics.apigateway.service.ResponseTransformerService;
import org.semantics.apigateway.service.configuration.ConfigurationLoader;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


@Service
public class ArtefactsService extends AbstractEndpointService {

    private final TerminologyStatisticRepository terminologyStatisticRepository;

    private static final String STATISTICS_FIELD = "statistics";
    private static final List<String> URI_KEYS = List.of("@id", "iri", "uri", "short_form");

    public ArtefactsService(ConfigurationLoader configurationLoader, CacheManager cacheManager, JsonLdTransform transform, ResponseTransformerService responseTransformerService, CollectionService collectionService, TerminologyStatisticRepository terminologyStatisticRepository) {
        super(configurationLoader, cacheManager, transform, responseTransformerService, collectionService, SemanticArtefact.class);
        this.terminologyStatisticRepository = terminologyStatisticRepository;
    }

    public Object getArtefacts(CommonRequestParams params, User currentUser, ApiAccessor accessor) {
        String endpoint = "resources";
        try {
            return
                    findAllArtefacts(params, currentUser, accessor)
                            .thenApply(data -> transformJsonLd(data, params))
                            .thenApply(data -> addTerminologyStatistics(data, params))
                            .thenApply(data -> transformForTargetDbSchema(data, params.getTargetDbSchema(), endpoint)).get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }


    public Object getArtefact(String id, CommonRequestParams params, ApiAccessor accessor, User currentUser) {
        try {
            AggregatedApiResponse result = findUriBase(id, null, "resource_details", params, accessor, currentUser)
                    .thenApply(data -> addTerminologyStatistics(data, params))
                    .thenApply(data -> transformForTargetDbSchema(data, params.getTargetDbSchema(), "resource_details", false))
                    .get();
            return result == null ? null : filterOutArtefactsWithoutProperIri(result, params);
        } catch (InterruptedException | ExecutionException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }


    public Object searchMetadata(String query, CommonRequestParams params, ApiAccessor accessor) {
        String endpoint = "resources";
        return findAllArtefacts(params, null, accessor)
                .thenApply(data -> filterOutByQuery(query, data))
                .thenApply(x -> transformJsonLd(x, params))
                .thenApply(data -> addTerminologyStatistics(data, params))
                .thenApply(data -> transformForTargetDbSchema(data, params.getTargetDbSchema(), endpoint));
    }

    private AggregatedApiResponse addTerminologyStatistics(AggregatedApiResponse data, CommonRequestParams params) {
        if (data == null || data.getCollection() == null || data.getCollection().isEmpty()) {
            return data;
        }

        Map<String, List<TerminologyStatistic>> latestByUri = terminologyStatisticRepository.findLatest().stream()
                .collect(Collectors.groupingBy(TerminologyStatistic::getUri));

        data.setCollection(data.getCollection().stream()
                .map(item -> withStatistics(item, latestByUri, params))
                .toList());
        return data;
    }

    private Map<String, Object> withStatistics(Map<String, Object> item, Map<String, List<TerminologyStatistic>> latestByUri, CommonRequestParams params) {
        if (item == null) {
            return null;
        }

        Object uri = URI_KEYS.stream().map(item::get).filter(Objects::nonNull).findFirst().orElse(null);
        if (uri == null) {
            return item;
        }

        Object source = item.get("source_name");
        List<TerminologyStatisticDto> statistics = latestByUri
                .getOrDefault(uri.toString(), List.of())
                .stream()
                .filter(s -> source == null || s.getSource().equals(source.toString()))
                .map(TerminologyStatisticDto::from)
                .toList();

        if (!params.isDisplayEmptyValues() && statistics.isEmpty()) {
            return item;
        }

        Map<String, Object> copy = new LinkedHashMap<>(item);
        copy.put(STATISTICS_FIELD, statistics);
        return copy;
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
    
    private AggregatedApiResponse filterOutArtefactsWithoutProperIri(AggregatedApiResponse data, CommonRequestParams params) {
        if (params.isOmitArtefactsWithoutIri()) {
            data.setCollection(data.getCollection().stream().filter(result ->
                    result.get("@id").toString().startsWith("https://") ||
                            result.get("@id").toString().startsWith("http://")
            ).toList());
        }
        return data;
    }

    private CompletableFuture<AggregatedApiResponse> findAllArtefacts(CommonRequestParams params, User currentUser, ApiAccessor accessor) {
        String endpoint = "resources";
        String database = params.getDatabase();
        TerminologyCollection collection = collectionService.getCurrentUserCollection(params.getCollectionId(), currentUser);

        accessor = initAccessor(database, endpoint, accessor, params.getTimeout());
        accessor = applyCollection(accessor, collection, endpoint);

        return accessor.get()
                .thenApply(data -> this.transformApiResponses(data, endpoint, new HashMap<>()))
                .thenApply(transformedData -> flattenResponseList(transformedData, params, collection))
                .thenApply(data -> filterOutArtefactsWithoutProperIri(data, params))
                .thenApply(data -> filterOutByCollection(collection, data));
    }
}
