package org.semantics.apigateway.api;

import org.semantics.apigateway.config.ResponseMapping;
import org.semantics.apigateway.model.responses.PaginatedResponse;
import org.semantics.apigateway.service.MappingTransformer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModTransformer implements DatabaseTransformer {
    public String toSnakeCaseRegex(String camelCase) {
        if (camelCase == null || camelCase.isEmpty()) {
            return camelCase;
        }

        // Replace uppercase letters preceded by a lowercase letter with '_' + lowercase

        return camelCase.replaceAll(
                "(?<=[a-z0-9])(?=[A-Z])",
                "_"
        ).toLowerCase();
    }


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

        return transformedItem;
    }

    @Override
    public Map<String, Object> constructResponse(List<Map<String, Object>> transformedResults, boolean list) {
        PaginatedResponse paginatedResponse = new PaginatedResponse(
                (long) transformedResults.size(),
                1,
                transformedResults
        );

        Map<String, Object> response = new HashMap<>();
        response.put("page", 1);
        response.put("pageCount", paginatedResponse.getTotalPages());
        response.put("totalCount", transformedResults.size());
        response.put("collection", transformedResults);
        response.put("@context", paginatedResponse.getContext());
        response.put("@type", paginatedResponse.getType());
        response.put("links", paginatedResponse.view());
        response.put("@id", paginatedResponse.id());
        return response;
    }
}
