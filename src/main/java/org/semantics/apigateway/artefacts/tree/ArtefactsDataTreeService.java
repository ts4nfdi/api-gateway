package org.semantics.apigateway.artefacts.tree;

import org.semantics.apigateway.config.DatabaseConfig;
import org.semantics.apigateway.model.CommonRequestParams;
import org.semantics.apigateway.model.Endpoints;
import org.semantics.apigateway.model.RDFResource;
import org.semantics.apigateway.model.responses.AggregatedApiResponse;
import org.semantics.apigateway.model.responses.AggregatedResourceBody;
import org.semantics.apigateway.service.AbstractEndpointService;
import org.semantics.apigateway.service.ApiAccessor;
import org.semantics.apigateway.service.JsonLdTransform;
import org.semantics.apigateway.service.ResponseTransformerService;
import org.semantics.apigateway.service.configuration.ConfigurationLoader;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


@Service
public class ArtefactsDataTreeService extends AbstractEndpointService {


    public ArtefactsDataTreeService(ConfigurationLoader configurationLoader, CacheManager cacheManager, JsonLdTransform transform, ResponseTransformerService responseTransformerService) {
        super(configurationLoader, cacheManager, transform, responseTransformerService, RDFResource.class);
    }

    public Object getRoots(String acronym, CommonRequestParams params, ApiAccessor accessor) {
        if (acronym == null || acronym.isEmpty()) {
            return new AggregatedApiResponse();
        }
        String endpoint = Endpoints.concepts_roots.toString();
        accessor = initAccessor(params.getDatabase(), endpoint, accessor);
        return findAll(acronym, endpoint, params, accessor).thenApply(x -> {
            x.setCollection(sortChildren((List<Map<String, Object>>) x.getCollection()));
            return x;
        });
    }

    public Object getChildren(String acronym, String uri, CommonRequestParams params, Integer page, ApiAccessor accessor) {
        if (acronym == null || acronym.isEmpty() || uri == null || uri.isEmpty()) {
            return Collections.EMPTY_LIST;
        }

        String endpoint = Endpoints.concepts_children.toString();
        DatabaseConfig databaseConfig = configurationLoader.getConfigByName(params.getDatabase());
        accessor = initAccessor(params.getDatabase(), endpoint, accessor);
        if (databaseConfig.isOls2()) {
            uri = URLEncoder.encode(uri); // OLS2 requires URI double encoding
        }
        return paginatedList(acronym, uri, endpoint, params, page, accessor);
    }

    public Object getTree(String acronym, String uri, CommonRequestParams params, ApiAccessor accessor) {
        if (acronym == null || acronym.isEmpty() || uri == null || uri.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        String endpoint = Endpoints.concept_tree.toString();
        DatabaseConfig databaseConfig = configurationLoader.getConfigByName(params.getDatabase());
        accessor = initAccessor(params.getDatabase(), endpoint, accessor);
        String finalUri = uri;
        if (databaseConfig.isOls2()) {
            finalUri = URLEncoder.encode(uri); // OLS2 requires URI double encoding
        }

        return findAll(acronym, finalUri, endpoint, params, accessor)
                .thenApply(data -> databaseConfig.isOls() ? buildOlsTree(data, acronym, uri, params) : data)
                .thenApply(data -> databaseConfig.isOntoPortal() ? transformAllNestedChildren(data, databaseConfig, endpoint) : data);
    }

    private AggregatedApiResponse buildOlsTree(AggregatedApiResponse data, String acronym, String uri, CommonRequestParams params) {
        List<Map<String, Object>> pathToRoot = data.getCollection();
        List<Map<String, Object>> cleanedPath = new ArrayList<>();
        Map<String, Object> leafNode = findUri(acronym, uri, Endpoints.concept_details.toString(), params, null).getCollection().get(0);

        for (Map<String, Object> child : pathToRoot) {
            if (child.get("iri").equals("http://www.w3.org/2002/07/owl#Thing")) {
                continue; // Skip the root node (owl:Thing)
            }

            child.put("children", cleanedPath.isEmpty() ? Collections.singletonList(leafNode) : Collections.singletonList(cleanedPath.get(cleanedPath.size() - 1)));
            cleanedPath.add(child);
        }

        data.setCollection(cleanedPath.isEmpty() ? Collections.emptyList() : Collections.singletonList(cleanedPath.get(cleanedPath.size() - 1)));
        return data;
    }

    private AggregatedApiResponse transformAllNestedChildren(AggregatedApiResponse data, DatabaseConfig databaseConfig, String endpoint) {
        List<Map<String, Object>> items = data.getCollection();
        items.forEach(item -> {
            List<Map<String, Object>> transformedChildren = transformNestedChildren((List<Map<String, Object>>) item.get("children"), databaseConfig, endpoint);
            item.put("children", transformedChildren);
        });
        data.setCollection(items);
        return data;
    }

    private List<Map<String, Object>> transformNestedChildren(List<Map<String, Object>> children, DatabaseConfig databaseConfig, String endpoint) {
        if (children == null || children.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> transformedChildren = new ArrayList<>();

        children.forEach(child -> {

            List<AggregatedResourceBody> transformedData = aggregatorTransformer.transformData(child, databaseConfig, endpoint);
            if (transformedData == null || transformedData.isEmpty()) {
                return;
            }

            Map<String, Object> transformedChild = transformedData.get(0).toMap(false, true);
            transformedChild.put("children", transformNestedChildren((List<Map<String, Object>>) transformedChild.get("children"), databaseConfig, endpoint));
            transformedChildren.add(transformedChild);
        });

        return sortChildren(transformedChildren);
    }

    private List<Map<String, Object>> sortChildren(List<Map<String, Object>> children) {
        if (children == null || children.isEmpty()) {
            return Collections.emptyList();
        }
        children.sort((a, b) -> {
            String labelA = a.get("label") != null ? a.get("label").toString() : a.get("iri").toString();
            String labelB = b.get("label") != null ? b.get("label").toString() : b.get("iri").toString();
            return labelA.compareToIgnoreCase(labelB);
        });
        return children;
    }
}

