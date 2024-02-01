package org.semantics.nfdi.api;

import org.semantics.nfdi.api.DatabaseTransformer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OlsTransformer implements DatabaseTransformer {
    @Override
    public Map<String, Object> transformItem(Map<String, Object> item) {
        if (item == null) {
            return null;
        }

        Map<String, Object> transformedItem = new HashMap<>();

        // Check for null values before accessing properties
        if (item.containsKey("iri") && item.get("iri") != null) {
            transformedItem.put("iri", item.get("iri"));
        }
        if (item.containsKey("label") && item.get("label") != null) {
            transformedItem.put("label", item.get("label"));
        }
        if (item.containsKey("synonym") && item.get("synonym") != null) {
            transformedItem.put("short_form", item.get("synonym"));
        }
        if (item.containsKey("ontology") && item.get("ontology") != null) {
            transformedItem.put("ontology_name", item.get("ontology"));
        }
        if (item.containsKey("description") && item.get("description") != null) {
            transformedItem.put("description", item.get("description"));
        }
        if (item.containsKey("source") && item.get("source") != null) {
            transformedItem.put("source", item.get("source"));
        }
        return transformedItem;
    }

    @Override
    public Map<String, Object> constructResponse(List<Map<String, Object>> transformedResults) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> innerResponse = new HashMap<>();
        List<Map<String, Object>> docs = new ArrayList<>();

        // Construct the "docs" list
        for (Map<String, Object> transformedResult : transformedResults) {
            Map<String, Object> doc = new HashMap<>();
            doc.putAll(transformedResult);
            docs.add(doc);
        }

        innerResponse.put("docs", docs);
        innerResponse.put("numFound", transformedResults.size());
        innerResponse.put("start", 0);

        response.put("response", innerResponse);

        Map<String, Object> responseHeader = new HashMap<>();
        responseHeader.put("QTime", 0);
        responseHeader.put("status", 0);

        response.put("responseHeader", responseHeader);

        return response;
    }
}