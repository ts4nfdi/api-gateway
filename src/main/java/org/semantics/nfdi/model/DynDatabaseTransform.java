package org.semantics.nfdi.model;

import org.semantics.nfdi.api.BioportalTransformer;
import org.semantics.nfdi.api.DatabaseTransformer;
import org.semantics.nfdi.api.OlsTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.stream.Collectors;

public class DynDatabaseTransform {
    private static final Logger logger = LoggerFactory.getLogger(DynDatabaseTransform.class);
    private Map<String, String> fieldMapping;
    private Map<String, Object> jsonSchema;
    private Map<String, String> responseMapping;
    private DatabaseTransformer olsTransformer;
    private DatabaseTransformer bioportalTransformer;

    // Constructor initializes the field mappings, JSON schema, response mappings, and transformers
    public DynDatabaseTransform(Map<String, String> fieldMapping, Map<String, Object> jsonSchema, Map<String, String> responseMapping) {
        this.fieldMapping = fieldMapping;
        this.jsonSchema = jsonSchema;
        this.responseMapping = responseMapping != null ? responseMapping : new HashMap<>();
        logger.info("Loaded JSON Schema: {}", jsonSchema);
        this.olsTransformer = new OlsTransformer();
        this.bioportalTransformer = new BioportalTransformer();
    }

    // Method to transform the JSON response from a database into a specific format
    public Map<String, Object> transformJsonResponse(List<Map<String, Object>> originalResponse, String targetDataBase) {
        Map<String, Object> response = new HashMap<>();
        switch (targetDataBase) {
            // Case for transforming data from the OLS database
            case "ols":
                List<Map<String, Object>> transformedResults = originalResponse.stream()
                        .map(olsTransformer::transformItem)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                response = olsTransformer.constructResponse(transformedResults);
                break;
            // Case for transforming data from the BioPortal database
            case "bioportal":
                List<Map<String, Object>> transformedResultsBioportal = originalResponse.stream()
                        .map(bioportalTransformer::transformItem)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                response = bioportalTransformer.constructResponse(transformedResultsBioportal);
                break;

            // Add more cases here for other databases as needed

            default:
                response.put("error", "No database configuration found");
                break;
        }
        return response;
    }
}