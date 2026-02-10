package org.semantics.apigateway.api;

import org.semantics.apigateway.config.ResponseMapping;
import org.semantics.apigateway.model.SemanticArtefact;
import org.semantics.apigateway.service.MappingTransformer;

import java.util.*;
import java.util.stream.Collectors;

public class OlsV2Transformer implements DatabaseTransformer {
    @Override
    public Map<String, Object> transformItem(Map<String, Object> item, ResponseMapping mapping) {
        if (item == null) {
            return null;
        }

        Map<String, Object> transformedItem = new HashMap<>();
        if (mapping == null) {
            return item;
        }

        mapping.inverseMapping().entrySet().stream().filter(entry -> !Set.of("totalCount", "page", "nestedJson", "key").contains(entry.getValue())).forEach(entry -> {
            
            String transformedKey = entry.getKey();
            String ourKey = entry.getValue();
            
            Object value = getNestedValue(item, ourKey);
            if (value == null) {
                value = item.get(toSnakeCaseRegex(ourKey));
            } else if (ourKey.equals("type")) {
                value = List.of(value.toString());
            }

            if (value != null ) {
                MappingTransformer.itemValueSetter(transformedItem, transformedKey, value);
            }
        });

        transformedItem.put("URI", item.get("iri"));
        transformedItem.put("@type", new SemanticArtefact().getTypeURI());
        return transformedItem;
    }


    @Override
    public Map<String, Object> constructResponse(List<Map<String, Object>> transformedResults, String mappingKey, boolean list, boolean paginate, int page, long totalCount) {
        if (list) {
            transformedResults = transformedResults.stream().filter(x -> x != null).collect(Collectors.toUnmodifiableList());
            Map<String, Object> response = new HashMap<>();
            response.put("elements", transformedResults);
            response.put("totalElements", transformedResults.size());
            response.put("numElements", transformedResults.size());
            response.put("page", page == 0 ? 0 : page - 1);
            return response;
        }
        
        return transformedResults.isEmpty() ? null : transformedResults.get(0);
    }
    
    private Object getNestedValue(Map<String, Object> item, String key) {
        String[] path = key.split("->");
        Object current = item;
        for(String pathSegment : Arrays.stream(path).limit(path.length - 1).toList()) {
            if (!(current instanceof Map)) {
                return null;
            }
            current = ((Map<String, Object>)current).get(pathSegment);
        }
        return ((Map<String, Object>)current).get(path[path.length - 1]);
    }
}
