package org.semantics.nfdi.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BioportalTransformer implements DatabaseTransformer {
    @Override
    public Map<String, Object> transformItem(Map<String, Object> item) {
        if (item == null) {
            return null;
        }

        Map<String, Object> transformedItem = new HashMap<>();

        // Check for null values before accessing properties
        if (item.containsKey("@id") && item.get("@id") != null) {
            transformedItem.put("iri", item.get("@id"));
        }
        if (item.containsKey("label") && item.get("label") != null) {
            transformedItem.put("prefLabel", item.get("label"));
        }
        if (item.containsKey("ontology") && item.get("ontology") != null) {
            transformedItem.put("@type", item.get("ontology"));
        }
        if (item.containsKey("synonym") && item.get("synonym") != null) {
            transformedItem.put("synonym", item.get("synonym"));
        }
        if (item.containsKey("description") && item.get("description") != null) {
            transformedItem.put("definition", item.get("description"));
        }
        if (item.containsKey("source") && item.get("source") != null) {
            transformedItem.put("source", item.get("source"));
        }
        return transformedItem;
    }

    @Override
    public Map<String, Object> constructResponse(List<Map<String, Object>> transformedResults) {
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