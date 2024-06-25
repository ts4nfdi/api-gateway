package org.semantics.apigateway.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.rdf.model.ResourceFactory;
import org.semantics.apigateway.api.DatabaseTransformer;

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
            // the value of the key @type in OntoPortal is saved as an IRI
            if (item.containsKey("backend_type") && String.valueOf(item.get("backend_type")).equals("ontoportal")) {
                transformedItem.put("short_form",
                        ResourceFactory.createResource(String.valueOf(item.get("iri"))).getLocalName().toLowerCase());
            }
        }
        if (item.containsKey("label") && item.get("label") != null) {
            transformedItem.put("label", item.get("label"));
        }
        if (item.containsKey("synonym") && item.get("synonym") != null) {
            transformedItem.put("synonym", item.get("synonym"));
        }
        if (item.containsKey("short_form") && item.get("short_form") != null) {
            transformedItem.put("short_form", item.get("short_form"));
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
        if (item.containsKey("backend_type") && item.get("backend_type") != null) {
            transformedItem.put("backend_type", item.get("backend_type"));
        }
        if (item.containsKey("type") && item.get("type") != null) {
            // the value of the key @type in OntoPortal is saved as an IRI
            if (item.containsKey("backend_type") && String.valueOf(item.get("backend_type")).equals("ontoportal")) {
                transformedItem.put("type",
                        ResourceFactory.createResource(String.valueOf(item.get("type"))).getLocalName().toLowerCase());
            } else {
                transformedItem.put("type", item.get("type"));
            }
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
