package org.semantics.nfdi.model;

import org.semantics.nfdi.config.DatabaseSchemaConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DynDatabaseTransform {

    private final DatabaseSchemaConfig databaseSchemaConfig;
    private static final Logger logger = LoggerFactory.getLogger(DynDatabaseTransform.class);

    public DynDatabaseTransform(DatabaseSchemaConfig databaseSchemaConfig) {
        this.databaseSchemaConfig = databaseSchemaConfig;
    }

    public Map<String, Object> transformDatabaseResponse(String targetSchema, List<Map<String, Object>> response) {
        try {
            Map<String, Object> dbConfig = (Map<String, Object>) databaseSchemaConfig.getDatabases().get(targetSchema);
            if (dbConfig == null) {
                logger.error("No configuration found for target schema: {}", targetSchema);
                throw new IllegalArgumentException("No configuration found for target schema: " + targetSchema);
            }

            Map<String, String> fieldMappings = (Map<String, String>) dbConfig.get("mapping");
            if (fieldMappings == null) {
                logger.error("Field mappings are null for target schema: {}", targetSchema);
                throw new IllegalStateException("Field mappings are null for target schema: " + targetSchema);
            }

            Map<String, Object> responseStructure = (Map<String, Object>) dbConfig.get("responseStructure");
            if (responseStructure == null) {
                logger.error("Response structure is null for target schema: {}", targetSchema);
                throw new IllegalStateException("Response structure is null for target schema: " + targetSchema);
            }

            List<Map<String, Object>> transformedDocs = transformDocs(response, fieldMappings);
            return wrapResponse(transformedDocs, responseStructure);
        } catch (Exception e) {
            logger.error("Error in transformDatabaseResponse: {}", e.getMessage(), e);
            throw e;
        }
    }

    private List<Map<String, Object>> transformDocs(List<Map<String, Object>> docs, Map<String, String> fieldMappings) {
        List<Map<String, Object>> transformedDocs = new ArrayList<>();
        for (Map<String, Object> doc : docs) {
            Map<String, Object> transformedDoc = new HashMap<>();
            fieldMappings.forEach((newKey, oldKey) -> {
                if (doc.containsKey(oldKey)) {
                    transformedDoc.put(newKey, doc.get(oldKey));
                }
            });
            transformedDocs.add(transformedDoc);
        }
        return transformedDocs;
    }

    private Map<String, Object> wrapResponse(List<Map<String, Object>> transformedDocs, Map<String, Object> responseStructure) {
        Map<String, Object> wrappedResponse = new HashMap<>();
        String topLevelKey = (String) responseStructure.get("topLevel");
        Map<String, Object> staticFields = (Map<String, Object>) responseStructure.get("staticFields");

        insertAtTopLevel(wrappedResponse, topLevelKey, transformedDocs);
        staticFields.forEach((key, value) -> {
            if (value instanceof String && ((String) value).contains("${size}")) {
                value = transformedDocs.size();
            }
            wrappedResponse.put(key, value);
        });

        return wrappedResponse;
    }

    private void insertAtTopLevel(Map<String, Object> response, String topLevelKey, List<Map<String, Object>> docs) {
        String[] keys = topLevelKey.split("\\.");
        Map<String, Object> currentLevel = response;
        for (int i = 0; i < keys.length - 1; i++) {
            currentLevel = (Map<String, Object>) currentLevel.computeIfAbsent(keys[i], k -> new HashMap<>());
        }
        currentLevel.put(keys[keys.length - 1], docs);
    }
}
