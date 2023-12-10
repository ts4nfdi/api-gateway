package org.semantics.nfdi.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseTransform {

    public Map<String, Object> transformDatabaseResponse(List<Map<String, Object>> olsResponse) {
        List<Map<String, Object>> transformedDocs = new ArrayList<>();

        for (Map<String, Object> item : olsResponse) {
            Map<String, Object> transformedItem = new HashMap<>();
            transformedItem.put("iri", item.get("iri"));
            transformedItem.put("ontology_prefix", item.get("ontology"));
            transformedItem.put("short_form", item.get("synonym"));
            transformedItem.put("description", item.get("description"));
            transformedItem.put("label", item.get("label"));
            transformedDocs.add(transformedItem);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("docs", transformedDocs);
        response.put("numFound", transformedDocs.size());
        response.put("start", 0);

        Map<String, Object> wrappedResponse = new HashMap<>();
        wrappedResponse.put("response", response);
        wrappedResponse.put("responseHeader", createResponseHeader());

        return wrappedResponse;
    }

    private Map<String, Object> createResponseHeader() {
        Map<String, Object> responseHeader = new HashMap<>();
        responseHeader.put("QTime", 0); // Placeholder for query time
        responseHeader.put("status", 0); // Placeholder for status
        return responseHeader;
    }
}
