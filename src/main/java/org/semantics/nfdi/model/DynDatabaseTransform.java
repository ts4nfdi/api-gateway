package org.semantics.nfdi.model;

import org.semantics.nfdi.service.DynSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hsqldb.HsqlDateTime.e;

public class DynDatabaseTransform {
    private static final Logger logger = LoggerFactory.getLogger(DynDatabaseTransform.class);
    private Map<String, String> fieldMapping;
    private Map<String, Object> jsonSchema;

    public DynDatabaseTransform(Map<String, String> fieldMapping, Map<String, Object> jsonSchema) {
        this.fieldMapping = fieldMapping;
        this.jsonSchema = jsonSchema;
        logger.info("Loaded JSON Schema: {}", jsonSchema);
    }

    public Map<String, Object> transformDatabaseResponse(List<Map<String, Object>> originalResponse) {
        List<Map<String, Object>> transformedResults = originalResponse.stream()
                .map(this::transformItem)
                .collect(Collectors.toList());

        Map<String, Object> finalResponse = new HashMap<>();
        finalResponse.put("docs", transformedResults);
        finalResponse.put("numFound", transformedResults.size());
        finalResponse.put("start", 0);

        Map<String, Object> response = new HashMap<>();
        response.put("response", finalResponse);

        return response;
    }




    private Map<String, Object> transformItem(Map<String, Object> item) {
        try {
            Map<String, Object> transformedItem = new HashMap<>();
            for (Map.Entry<String, String> mappingEntry : fieldMapping.entrySet()) {
                String sourceField = mappingEntry.getKey();
                String targetField = mappingEntry.getValue();
                if (item.containsKey(sourceField)) {
                    transformedItem.put(targetField, item.get(sourceField));
                }
            }

            logger.info("Transformed Database Item: {}", transformedItem);
            return transformedItem;
        } catch (Exception e) {
            logger.error("Error in transforming item: {}", e.getMessage(), e);
            throw e;
        }
    }

    private Map<String, Object> constructResponseBasedOnSchema(List<Map<String, Object>> transformedResults) {
        Map<String, Object> response = new HashMap<>();
        buildResponseStructure(response, jsonSchema, "root", transformedResults);
        logger.info("Final transformed response: {}", response);
        return response;
    }

    private void buildResponseStructure(Map<String, Object> response, Map<String, Object> schema,
                                        String currentKey, Object data) {
        try {
            if (schema.containsKey("type")) {
                String type = (String) schema.get("type");
                switch (type) {
                    case "object":
                        handleObjectType(response, schema, currentKey, data);
                        break;
                    case "array":
                        handleArrayType(response, schema, currentKey, data);
                        break;
                    default:
                        if (data != null) {
                            response.put(currentKey, data);
                        }
                        break;
                }
                logger.info("Building structure for key '{}', data: {}", currentKey, data);
            }
        } catch(Exception e){
            logger.error("Error in building response structure for key '{}': {}", currentKey, e.getMessage());
        }
    }

    private void handleObjectType(Map<String, Object> response, Map<String, Object> schema,
                                  String currentKey, Object data) {
        if (schema.containsKey("properties") && data instanceof Map<?, ?>) {
            Map<String, Object> properties = (Map<String, Object>) schema.get("properties");
            Map<String, Object> nestedResponse = new HashMap<>();
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                String key = entry.getKey();
                Map<String, Object> nestedSchema = (Map<String, Object>) entry.getValue();
                Object nestedData = ((Map<?, ?>) data).get(key);
                buildResponseStructure(nestedResponse, nestedSchema, key, nestedData);
                logger.info("Handling object type for key '{}', data: {}", currentKey, data);
            }
            response.put(currentKey, nestedResponse);
        }
    }

    private void handleArrayType(Map<String, Object> response, Map<String, Object> schema,
                                 String currentKey, Object data) {
        if (schema.containsKey("items") && data instanceof List<?>) {
            List<?> dataList = (List<?>) data;
            List<Object> arrayResponse = new ArrayList<>();
            Map<String, Object> itemSchema = (Map<String, Object>) schema.get("items");
            for (Object itemData : dataList) {
                Map<String, Object> itemResponse = new HashMap<>();
                buildResponseStructure(itemResponse, itemSchema, "item", itemData);
                arrayResponse.add(itemResponse.get("item"));
                logger.info("Handling array type for key '{}', data: {}", currentKey, data);
            }
            response.put(currentKey, arrayResponse);
        }
    }

    public List<Map<String, Object>> backTransformToDatabaseSchema(List<Map<String, Object>> transformedResponse) {
        return transformedResponse.stream()
                .map(this::backTransformItem)
                .collect(Collectors.toList());
    }

    private Map<String, Object> backTransformItem(Map<String, Object> transformedItem) {
        Map<String, Object> originalItem = new HashMap<>();
        fieldMapping.forEach((originalField, transformedField) -> {
            if (transformedItem.containsKey(transformedField)) {
                originalItem.put(originalField, transformedItem.get(transformedField));
            }
        });
        return originalItem;
    }
}
