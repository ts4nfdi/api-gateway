package org.semantics.apigateway.api;

import org.semantics.apigateway.config.ResponseMapping;
import org.semantics.apigateway.model.SemanticArtefact;
import org.semantics.apigateway.service.MappingTransformer;

import java.util.*;
import java.util.stream.Collectors;

public class OlsTransformer implements DatabaseTransformer {
    @Override
    public Map<String, Object> transformItem(Map<String, Object> item, ResponseMapping mapping) {
        if (item == null) {
            return null;
        }
      
        if (mapping == null) {
          return item;
        }
        
        if(mapping.getMappedClassAttributes().containsValue("autosuggest")) {
            return Map.of("autosuggest", item.get("label"));
        }

        Map<String, Object> transformedItem = new HashMap<>();

        mapping.inverseMapping().forEach((transformedKey, ourKey) -> {

            Object value = item.get(ourKey);
            if (value == null) {
                value = item.get(toSnakeCaseRegex(ourKey));
            }

            MappingTransformer.itemValueSetter(transformedItem, transformedKey, value);
        });
        
        Map<String, List<String>> annotations = (Map<String, List<String>>) transformedItem.get("annotation");
        if (annotations != null) {
            annotations = annotations.entrySet().stream().filter(e -> e.getValue() != null && !e.getValue().isEmpty()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            if (annotations.isEmpty()) {
                transformedItem.remove("annotation");
            } else {
                transformedItem.put("annotation", annotations);
            }
        }
        
        transformedItem.put("URI", item.get("iri"));
        transformedItem.put("@type", new SemanticArtefact().getTypeURI());
        return transformedItem;
    }


    @Override
    public Map<String, Object> constructResponse(List<Map<String, Object>> transformedResults, String mappingKey, boolean list, boolean paginate, int page, long totalCount) {
        Map<String, Object> response = new HashMap<>();
        
        if (!list && !"terms".equals(mappingKey)) {
          transformedResults.stream().findFirst().ifPresent(response::putAll);
          return response;
        }
        
        boolean autosuggest = transformedResults.stream().anyMatch(result -> result.containsKey("autosuggest"));
        
        Map<String, Object> innerResponse = new HashMap<>();
        List<Map<String, Object>> objectList = new ArrayList<>();

        // Construct the "docs" list
        for (Map<String, Object> transformedResult : transformedResults) {
          Map<String, Object> object = new HashMap<>(transformedResult);
          objectList.add(object);
        }

        innerResponse.put(mappingKey, objectList);
//        innerResponse.put("_links", "{}");
      
      if (!autosuggest) {
        var pageObject = new HashMap<String, Object>();
        pageObject.put("size", transformedResults.size());
        pageObject.put("totalElements", totalCount);
        pageObject.put("number", page == 0 ? 0 : page - 1);
        innerResponse.put("page", pageObject);
      }
      
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
