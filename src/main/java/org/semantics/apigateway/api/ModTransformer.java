package org.semantics.apigateway.api;

import org.semantics.apigateway.config.ResponseMapping;
import org.semantics.apigateway.model.SemanticArtefact;
import org.semantics.apigateway.model.responses.PaginatedResponse;
import org.semantics.apigateway.service.MappingTransformer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModTransformer implements DatabaseTransformer {

    @Override
    public Map<String, Object> transformItem(Map<String, Object> item, ResponseMapping mapping) {
        if (item == null) {
            return null;
        }

        Map<String, Object> transformedItem = new HashMap<>();
        if (mapping == null) {
            return item;
        }
        mapping.inverseMapping().forEach((transformedKey, ourKey) -> {

            Object value = item.get(ourKey);
            if (value == null) {
                value = item.get(toSnakeCaseRegex(ourKey));
            }

            MappingTransformer.itemValueSetter(transformedItem, transformedKey, value);
        });

        transformedItem.put("URI", item.get("iri"));
        transformedItem.put("@type", new SemanticArtefact().getTypeURI());
        return transformedItem;
    }

    @Override
    public Map<String, Object> constructResponse(List<Map<String, Object>> transformedResults, String mappingKey, boolean list) {
        PaginatedResponse paginatedResponse = new PaginatedResponse(
                transformedResults,
                transformedResults.size(),
                1
        );

        Map<String, Object> response = new HashMap<>();
        response.put("page", 1);
        response.put("pageCount", paginatedResponse.getTotalPages());
        response.put("totalItems", transformedResults.size());
        response.put("member", transformedResults);
        response.put("@context", paginatedResponse.getContext());
        response.put("@type", paginatedResponse.getType());
        response.put("links", paginatedResponse.view());
        response.put("@id", paginatedResponse.id());
        response.put("view", paginatedResponse.view());
        return response;
    }
}
