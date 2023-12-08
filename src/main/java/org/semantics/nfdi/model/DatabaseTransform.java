package org.semantics.nfdi.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semantics.nfdi.config.OntologyConfig;
import org.semantics.nfdi.model.DynTransformResponse;

public class DatabaseTransform {
    private static final Set<String> OLS_SCHEMA_KEYS = new HashSet<>(Arrays.asList(
        "iri", "ontology_prefix", "short_form", "description", "label"
    ));


    public List<Map<String, Object>> transformDatabaseResponse(List<Map<String, Object>> combinedResults) {
        List<Map<String, Object>> transformedDocs = new ArrayList<>();

        for (Map<String, Object> item : combinedResults) {
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

        List<Map<String, Object>> result = new ArrayList<>();
        result.add(response);
        return result;
    }
}