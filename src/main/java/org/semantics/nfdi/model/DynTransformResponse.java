package org.semantics.nfdi.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.semantics.nfdi.config.OntologyConfig;
import org.semantics.nfdi.config.ResponseMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DynTransformResponse {

    private static final Logger logger = LoggerFactory.getLogger(DynTransformResponse.class);

    // Method to dynamically transform a response based on the provided OntologyConfig
    public List<Map<String, Object>> dynTransformResponse(Map<String, Object> response, OntologyConfig config) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (response == null) {
            logger.error("Response is null");
            return result;
        }

        // Extracting the nested JSON key from the response based on the configuration
        String nestedJsonKey = config.getResponseMapping().getNestedJson();
        Object nestedData = response.getOrDefault(nestedJsonKey, new ArrayList<>());
        logger.info("Nested JSON key: {}", nestedJsonKey);
        logger.info("Nested data type: {}", nestedData.getClass().getSimpleName());
        
        // Processing the data based on its type (List or Map)
        if (nestedData instanceof List) {
            processList((List<?>) nestedData, result, config);
        } else if (nestedData instanceof Map) {
            // Extracting the documents from the nested data
            String docsKey = config.getResponseMapping().getKey();
            Object docs = ((Map<?, ?>) nestedData).get(docsKey);
            if (docs instanceof List) {
                processList((List<?>) docs, result, config);
            } else {
                logger.error("Expected List for key '{}', but found: {}", docsKey, docs.getClass().getSimpleName());
            }
        } else {
            logger.error("Expected List or Map for nested JSON key: {}, but found: {}", nestedJsonKey, nestedData.getClass().getSimpleName());
        }

        logger.info("Transformed response: {}", result);
        return result;
    }

    // Helper method to process a list of data items
    private void processList(List<?> dataList, List<Map<String, Object>> result, OntologyConfig config) {
        for (Object item : dataList) {
            if (item instanceof Map) {
                // Processing each item in the list
                Map<String, Object> newItem = processItem((Map<String, Object>) item, config);
                if (!newItem.isEmpty()) {
                    result.add(newItem);
                }
            }
        }
    }

    // Method to process individual items in the response
    private Map<String, Object> processItem(Map<String, Object> item, OntologyConfig config) {
        Map<String, Object> newItem = new HashMap<>();
        if (item == null) {
            logger.error("Item is null");
            return newItem;
        }

        ResponseMapping responseMapping = config.getResponseMapping();

        // Mapping fields based on the YAML configuration
        try {
            if (responseMapping.getIri() != null && item.containsKey(responseMapping.getIri())) {
                newItem.put("iri", item.get(responseMapping.getIri()));
            }
            if (responseMapping.getLabel() != null && item.containsKey(responseMapping.getLabel())) {
                newItem.put("label", item.get(responseMapping.getLabel()));
            }
            if (responseMapping.getSynonym() != null && item.containsKey(responseMapping.getSynonym())) {
                newItem.put("synonym", item.get(responseMapping.getSynonym()));
            }
            if (responseMapping.getDescription() != null && item.containsKey(responseMapping.getDescription())) {
                newItem.put("description", item.get(responseMapping.getDescription()));
            }
            if (responseMapping.getOntology() != null && item.containsKey(responseMapping.getOntology())) {
                newItem.put("ontology", item.get(responseMapping.getOntology()));
            }
            // Adding the source database as part of the new item
            newItem.put("source", config.getDatabase());

        } catch (Exception e) {
            logger.error("Error processing item: {}", e.getMessage(), e);
        }
        // logger.info("Transformed item: {}", newItem);
        return newItem;
    }


}
