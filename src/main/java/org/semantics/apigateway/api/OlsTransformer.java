package org.semantics.apigateway.api;

import org.semantics.apigateway.config.ResponseMapping;
import org.semantics.apigateway.model.SemanticArtefact;
import org.semantics.apigateway.service.MappingTransformer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OlsTransformer implements DatabaseTransformer {
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
        Map<String, Object> response = new HashMap<>();
        
        if (!list) {
          transformedResults.stream().findFirst().ifPresent(response::putAll);
          return response;
        }
        
        Map<String, Object> innerResponse = new HashMap<>();
        List<Map<String, Object>> objectList = new ArrayList<>();

        // Construct the "docs" list
        for (Map<String, Object> transformedResult : transformedResults) {
          Map<String, Object> object = new HashMap<>(transformedResult);
          objectList.add(object);
        }

        innerResponse.put(mappingKey, objectList);
        innerResponse.put("_links", "{}"); // TODO retrieve links and add here
        innerResponse.put("page", "{}"); // TODO handle pagination and add info here

        response.put(mappingKey.equals("docs") ? "response" : "_embedded", innerResponse);

        if (mappingKey.equals("docs")) {
          Map<String, Object> responseHeader = new HashMap<>();
          responseHeader.put("QTime", 0);
          responseHeader.put("status", 0);
          response.put("responseHeader", responseHeader);
        }
        
        return response;
    }
}
