package org.semantics.apigateway.api;

import org.semantics.apigateway.config.ResponseMapping;
import org.semantics.apigateway.model.SemanticArtefact;
import org.semantics.apigateway.service.MappingTransformer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        if (list) {
            Map<String, Object> response = new HashMap<>();
            response.put("elements", transformedResults);
            response.put("totalElements", transformedResults.size());
            response.put("numElements", transformedResults.size());
            response.put("page", 0);
            return response;
        }
        
        return transformedResults.getFirst();
    }
}
