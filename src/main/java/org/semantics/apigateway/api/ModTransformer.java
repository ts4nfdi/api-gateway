package org.semantics.apigateway.api;

import org.semantics.apigateway.config.ResponseMapping;
import org.semantics.apigateway.service.MappingTransformer;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModTransformer implements DatabaseTransformer {
    public String toSnakeCaseRegex(String camelCase) {
        if (camelCase == null || camelCase.isEmpty()) {
            return camelCase;
        }

        // Replace uppercase letters preceded by a lowercase letter with '_' + lowercase
        String snakeCase = camelCase.replaceAll(
                "(?<=[a-z0-9])(?=[A-Z])",
                "_"
        ).toLowerCase();

        return snakeCase;
    }


    @Override
    public Map<String, Object> transformItem(Map<String, Object> item, ResponseMapping mapping) {
        if (item == null) {
            return null;
        }

        Map<String, Object> transformedItem = new HashMap<>();
        if(mapping == null){
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
        if(!list) {
            Map<String, Object> response = transformedResults.get(0);
            response.put("@context", Collections.singletonMap("@vocab", ""));
            return response;
        }

        Map<String, Object> response = new HashMap<>();
        response.put("page", 1);
        response.put("pageCount", 1);
        response.put("totalCount", transformedResults.size());
        response.put("prevPage", null);
        response.put("nextPage", null);
        Map<String, Object> links = new HashMap<>();
        links.put("nextPage", null);
        links.put("prevPage", null);
        response.put("links", links);
        response.put("collection", transformedResults);
        response.put("@context", Collections.singletonMap("@vocab", ""));
        return response;
    }
}
