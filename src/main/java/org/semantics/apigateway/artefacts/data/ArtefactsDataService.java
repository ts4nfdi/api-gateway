package org.semantics.apigateway.artefacts.data;

import org.semantics.apigateway.collections.CollectionService;
import org.semantics.apigateway.model.CommonRequestParams;
import org.semantics.apigateway.model.RDFResource;
import org.semantics.apigateway.model.TargetDbSchema;
import org.semantics.apigateway.model.responses.AggregatedApiResponse;
import org.semantics.apigateway.model.responses.ApiResponse;
import org.semantics.apigateway.model.user.User;
import org.semantics.apigateway.service.AbstractEndpointService;
import org.semantics.apigateway.service.ApiAccessor;
import org.semantics.apigateway.service.JsonLdTransform;
import org.semantics.apigateway.service.ResponseTransformerService;
import org.semantics.apigateway.service.configuration.ConfigurationLoader;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


@Service
public class ArtefactsDataService extends AbstractEndpointService {


    public ArtefactsDataService(ConfigurationLoader configurationLoader, CacheManager cacheManager, JsonLdTransform transform, ResponseTransformerService responseTransformerService, CollectionService collectionService) {
        super(configurationLoader, cacheManager, transform, responseTransformerService, collectionService, RDFResource.class);
    }

    public Object getArtefactTerm(String id, String uri, CommonRequestParams params, ApiAccessor accessor, User currentUser) {
        return findUri(id, uri, "concept_details", params, accessor, currentUser);
    }

    public Object getArtefactTerms(String id,CommonRequestParams params, Integer page, ApiAccessor accessor, User currentUser) {
        return paginatedList(id, "concepts", params, page, accessor, currentUser);
    }

    public Object getArtefactProperty(String id, String uri, CommonRequestParams params, ApiAccessor accessor, User currentUser) {
        return findUri(id, uri, "property_details", params, accessor, currentUser);
    }

    public Object getArtefactProperties(String id, CommonRequestParams params, Integer page, ApiAccessor accessor, User currentUser) {
        return paginatedList(id, "properties", params, page, accessor, currentUser);
    }

    public Object getArtefactIndividual(String id, String uri, CommonRequestParams params, ApiAccessor accessor, User currentUser) {
        return findUri(id, uri, "individual_details", params, accessor, currentUser);
    }

    public Object getArtefactIndividuals(String id, CommonRequestParams params, Integer page, ApiAccessor accessor, User currentUser) {
        return paginatedList(id, "individuals", params, page, accessor, currentUser);
    }

    public Object getArtefactSchemes(String id, CommonRequestParams params, Integer page, ApiAccessor accessor, User currentUser) {
        return paginatedList(id, "schemes", params, page, accessor, currentUser);
    }

    public Object getArtefactScheme(String id, String uri, CommonRequestParams params, ApiAccessor accessor, User currentUser) {
        return findUri(id, uri, "scheme_details", params, accessor, currentUser);
    }

    public Object getArtefactCollections(String id, CommonRequestParams params, Integer page, ApiAccessor accessor, User currentUser) {
        return paginatedList(id, "collections", params, page, accessor, currentUser);
    }

    public Object getArtefactCollection(String id, String uri, CommonRequestParams params, ApiAccessor accessor, User currentUser) {
        return findUri(id, uri, "collection_details", params, accessor, currentUser);
    }

    public Object getArtefactEntities(String id, CommonRequestParams params, Integer page, ApiAccessor accessor, User currentUser) {
        TargetDbSchema targetDbSchema = params.getTargetDbSchema();

        CompletableFuture<AggregatedApiResponse> termsF = paginatedListRaw(id, null, "concepts", params, page, accessor, currentUser)
                .exceptionally(ex -> emptyOnError("concepts", ex));
        CompletableFuture<AggregatedApiResponse> propsF = paginatedListRaw(id, null, "properties", params, page, accessor, currentUser)
                .exceptionally(ex -> emptyOnError("properties", ex));
        CompletableFuture<AggregatedApiResponse> indivF = paginatedListRaw(id, null, "individuals", params, page, accessor, currentUser)
                .exceptionally(ex -> emptyOnError("individuals", ex));

        return CompletableFuture.allOf(termsF, propsF, indivF)
                .thenApply(v -> mergeAggregatedResponses(termsF.join(), propsF.join(), indivF.join()))
                .thenApply(merged -> transformForTargetDbSchema(merged, targetDbSchema, "entities", true));
    }

    public Object getArtefactEntity(String id, String uri, CommonRequestParams params, ApiAccessor accessor, User currentUser) {
        TargetDbSchema targetDbSchema = params.getTargetDbSchema();

        CompletableFuture<AggregatedApiResponse> conceptF = findUriRaw(id, uri, "concept_details", params, accessor, currentUser)
                .exceptionally(ex -> emptyOnError("concept_details", ex));
        CompletableFuture<AggregatedApiResponse> propF = findUriRaw(id, uri, "property_details", params, accessor, currentUser)
                .exceptionally(ex -> emptyOnError("property_details", ex));
        CompletableFuture<AggregatedApiResponse> indivF = findUriRaw(id, uri, "individual_details", params, accessor, currentUser)
                .exceptionally(ex -> emptyOnError("individual_details", ex));

        try {
            return CompletableFuture.allOf(conceptF, propF, indivF)
                    .thenApply(v -> mergeAggregatedResponses(conceptF.join(), propF.join(), indivF.join()))
                    .thenApply(merged -> transformForTargetDbSchema(merged, targetDbSchema, "entity_details", false))
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }


}

