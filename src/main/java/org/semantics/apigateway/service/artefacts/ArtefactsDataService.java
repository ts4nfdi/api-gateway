package org.semantics.apigateway.service.artefacts;

import org.semantics.apigateway.model.CommonRequestParams;
import org.semantics.apigateway.service.AbstractEndpointService;
import org.semantics.apigateway.service.ApiAccessor;
import org.semantics.apigateway.service.JsonLdTransform;
import org.semantics.apigateway.service.ResponseTransformerService;
import org.semantics.apigateway.service.configuration.ConfigurationLoader;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;


@Service
public class ArtefactsDataService extends AbstractEndpointService {


    public ArtefactsDataService(ConfigurationLoader configurationLoader, CacheManager cacheManager, JsonLdTransform transform, ResponseTransformerService responseTransformerService) {
        super(configurationLoader, cacheManager, transform, responseTransformerService);
    }

    public Object getArtefactTerm(String id, String uri, CommonRequestParams params, ApiAccessor accessor) {
        return findUri(id, uri, "concept_details", params, accessor);
    }

    public Object getArtefactTerms(String id,CommonRequestParams params, Integer page, ApiAccessor accessor) {
        return paginatedList(id, "concepts", params, page, accessor);
    }

    public Object getArtefactProperty(String id, String uri, CommonRequestParams params, ApiAccessor accessor) {
        return findUri(id, uri, "property_details", params, accessor);
    }

    public Object getArtefactProperties(String id, CommonRequestParams params, Integer page, ApiAccessor accessor) {
        return paginatedList(id, "properties", params, page, accessor);
    }

    public Object getArtefactIndividual(String id, String uri, CommonRequestParams params, ApiAccessor accessor) {
        return findUri(id, uri, "individual_details", params, accessor);
    }

    public Object getArtefactIndividuals(String id, CommonRequestParams params, Integer page, ApiAccessor accessor) {
        return paginatedList(id, "individuals", params, page, accessor);
    }


    public Object getArtefactSchemes(String id, CommonRequestParams params, Integer page, ApiAccessor accessor) {
        return paginatedList(id, "schemes", params, page, accessor);
    }

    public Object getArtefactScheme(String id, String uri, CommonRequestParams params, ApiAccessor accessor) {
        return findUri(id, uri, "scheme_details", params, accessor);
    }

    public Object getArtefactCollections(String id, CommonRequestParams params, Integer page, ApiAccessor accessor) {
        return paginatedList(id, "collections", params, page, accessor);
    }

    public Object getArtefactCollection(String id, String uri, CommonRequestParams params, ApiAccessor accessor) {
        return findUri(id, uri, "collection_details", params, accessor);
    }
}

