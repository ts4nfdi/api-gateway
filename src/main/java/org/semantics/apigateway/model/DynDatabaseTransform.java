package org.semantics.apigateway.model;

import org.semantics.apigateway.api.OntoPortalTransformer;
import org.semantics.apigateway.api.DatabaseTransformer;
import org.semantics.apigateway.api.OlsTransformer;
import org.semantics.apigateway.api.AgrovocTransformer;
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
    private DatabaseTransformer ontoPortalTransformer;
    private DatabaseTransformer agrovocTransformer;

    // Constructor initializes the field mappings, JSON schema, response mappings, and transformers
    public DynDatabaseTransform(Map<String, String> fieldMapping, Map<String, Object> jsonSchema, Map<String, String> responseMapping) {
        this.fieldMapping = fieldMapping;
        this.jsonSchema = jsonSchema;
        this.responseMapping = responseMapping != null ? responseMapping : new HashMap<>();
        logger.info("Loaded JSON Schema: {}", jsonSchema);
        this.olsTransformer = new OlsTransformer();
        this.ontoPortalTransformer = new OntoPortalTransformer();
        this.agrovocTransformer = new AgrovocTransformer();
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
            // Case for transforming data from the OntoPortal database
            case "ontoportal":
                List<Map<String, Object>> transformedResultsOntoPortal = originalResponse.stream()
                        .map(ontoPortalTransformer::transformItem)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                response = ontoPortalTransformer.constructResponse(transformedResultsOntoPortal);
                break;

            // Add more cases here for other databases as needed
            case "agrovoc":
                logger.info("agrovoc");
                List<Map<String, Object>> transformedResultsAgrovoc = originalResponse.stream()
                        .map(AgrovocTransformer::transformItem)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                logger.info(Collectors.toList());
                response = agrovocTransformer.constructResponse(transformedResultsAgrovoc);
                break;

            default:
                response.put("error", "No database configuration found");
                break;
        }
        return response;
    }
}
