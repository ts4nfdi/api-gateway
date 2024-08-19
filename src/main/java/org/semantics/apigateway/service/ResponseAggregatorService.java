package org.semantics.apigateway.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.rdf.model.ResourceFactory;
import org.semantics.apigateway.config.OntologyConfig;
import org.semantics.apigateway.config.ResponseMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ResponseAggregatorService {

    private static final Logger logger = LoggerFactory.getLogger(ResponseAggregatorService.class);

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

        // Mapping fields based on the JSON configuration
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

            if (responseMapping.getShortForm() != null && item.containsKey(responseMapping.getShortForm())) {
                newItem.put("short_form", item.get(responseMapping.getShortForm()));
            } else if (newItem.containsKey("iri") && newItem.get("iri") != null) {
                newItem.put("short_form",
                        ResourceFactory.createResource(String.valueOf(newItem.get("iri"))).getLocalName().toLowerCase());

            }

            if (responseMapping.getDescription() != null && item.containsKey(responseMapping.getDescription())) {
                List<String> list = (List<String>) item.get(responseMapping.getDescription());
                newItem.put("description", list);
            }
            if (responseMapping.getOntology() != null && item.containsKey(responseMapping.getOntology())) {
                if (responseMapping.getOntology().equals("links")) {
                    Object keysObject = ((Map<?, ?>) item).get(responseMapping.getOntology());
                    String ontologyItem = ((Map<?, String>) keysObject).get("ontology");
                    newItem.put("ontology", ResourceFactory.createResource(ontologyItem).getLocalName().toLowerCase());
                } else {
                    newItem.put("ontology", item.get(responseMapping.getOntology()));
                }
            }
            if (responseMapping.getType() != null && item.containsKey(responseMapping.getType())) {
                if (config.getDatabase().equals("ontoportal")) {
                    newItem.put("type", "class"); // ontoportal do the search only on classes for now
                } else if (config.getDatabase().equals("skosmos")) {
                    newItem.put("type", "individual"); // workaround ols type implementation that do not support skos types
                } else {
                    newItem.put("type", item.get(responseMapping.getType()));
                }
            }

            // Adding the source database as part of the new item
            if (String.valueOf(config.getUrl()).contains("/search?")) {
                newItem.put("source", String.valueOf(config.getUrl()).substring(0, String.valueOf(config.getUrl()).indexOf("/search?")));
            } else if (String.valueOf(config.getUrl()).contains("/select?")) {
                newItem.put("source", String.valueOf(config.getUrl()).substring(0, String.valueOf(config.getUrl()).indexOf("/select?")));
            } else {
                newItem.put("source", config.getUrl());
            }

            // Adding the backend database type as part of the new item
            newItem.put("backend_type", config.getDatabase());

        } catch (Exception e) {
            logger.error("Error processing item: {}", e.getMessage(), e);
        }
        // logger.info("Transformed item: {}", newItem);
        return newItem;
    }


}
