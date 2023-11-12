package org.semantics.nfdi.model;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;
import java.util.Map;

import org.semantics.nfdi.service.SearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TransformResponse {

    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);

    public List<Map<String, Object>> transformResponse(Map<String, Object> response, String source) {
    List<Map<String, Object>> result = new ArrayList<>();
    if (response == null) {
        return result;
    }

    // Handle 'response.docs' field
    Map<String, Object> responseMap = (Map<String, Object>) response.get("response");
    if (responseMap != null) {
        List<Object> docs = (List<Object>) responseMap.get("docs");
        if (docs != null) {
            for (Object doc : docs) {
                if (doc == null) {
                    continue;
                }

                Map<String, Object> docMap = (Map<String, Object>) doc;
                Map<String, Object> newItem = new HashMap<>();

                newItem.put("iri", docMap.get("iri"));
                newItem.put("Label", docMap.get("label"));
                newItem.put("source", docMap.get("ontology_name"));
                newItem.put("synonym", docMap.get("description"));
                newItem.put("ontology", docMap.get("id"));

                result.add(newItem);
            }
        }
    }

    // Handle 'collection' field
    List<Object> collection = (List<Object>) response.get("collection");
    if (collection != null) {
        for (Object item : collection) {
            if (item == null) {
                continue;
            }

            Map<String, Object> itemMap = (Map<String, Object>) item;
            Map<String, Object> newItem = new HashMap<>();

            Object iri = itemMap.get("@id");
            if (iri == null) {
                iri = itemMap.get("uri");
            }
            newItem.put("iri", iri);

            Map<String, Object> links = (Map<String, Object>) itemMap.get("links");
            if (links != null) {
                newItem.put("ontology", links.get("ontology"));
            }

            String getLabel = itemMap.get("prefLabel") != null ? (String) itemMap.get("prefLabel") : (String) itemMap.get("label");
            newItem.put("Label", getLabel);

            newItem.put("source", source);
            newItem.put("synonym", itemMap.get("synonym"));

            result.add(newItem);
        }
    }

    // Handle 'results' field
    List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
    if (results != null) {
        for (Map<String, Object> item : results) {
            if (item == null) {
                continue;
            }

            Map<String, Object> newItem = new HashMap<>();
            newItem.put("iri", item.get("uri"));
            newItem.put("Label", item.get("label"));
            newItem.put("description", item.get("description"));
            newItem.put("source", item.get("sourceTerminology"));
            newItem.put("internal", item.get("internal"));

            result.add(newItem);
        }
    }


    return result;
    }
    
}
    
