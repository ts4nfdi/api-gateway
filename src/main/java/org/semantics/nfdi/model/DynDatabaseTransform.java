package org.semantics.nfdi.model;

import org.semantics.nfdi.config.DatabaseConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.semantics.nfdi.config.DatabaseConfig;

@Component
public class DynDatabaseTransform {

    private final DatabaseConfig databaseConfig;

    public DynDatabaseTransform(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    public Map<String, Object> transformDatabaseResponse(String databaseName, List<Map<String, Object>> response) {
        List<Map<String, String>> mappings = databaseConfig.getMappings().get(databaseName);
        if (mappings == null) {
            throw new IllegalArgumentException("No mappings found for database: " + databaseName);
        }

        List<Map<String, Object>> transformedDocs = new ArrayList<>();
        for (Map<String, Object> item : response) {
            Map<String, Object> transformedItem = new HashMap<>();
            for (Map<String, String> mapping : mappings) {
                mapping.forEach((key, value) -> transformedItem.put(key, item.get(value)));
            }
            transformedDocs.add(transformedItem);
        }

        // Assuming the structure of the response is consistent across different databases
        Map<String, Object> wrappedResponse = new HashMap<>();
        wrappedResponse.put("response", Map.of("docs", transformedDocs, "numFound", transformedDocs.size(), "start", 0));
        wrappedResponse.put("responseHeader", Map.of("QTime", 0, "status", 0));

        return wrappedResponse;
    }
}
