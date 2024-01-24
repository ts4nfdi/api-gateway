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

    public DynDatabaseTransform(Map<String, String> fieldMapping, Map<String, Object> jsonSchema, Map<String, String> responseMapping) {
        this.fieldMapping = fieldMapping;
        this.jsonSchema = jsonSchema;
        this.responseMapping = responseMapping != null ? responseMapping : new HashMap<>();
        logger.info("Loaded JSON Schema: {}", jsonSchema);
        this.olsTransformer = new OlsTransformer();
        this.bioportalTransformer = new BioportalTransformer();
    }

    public Map<String, Object> transformJsonResponse(List<Map<String, Object>> originalResponse, String targetDataBase) {
        Map<String, Object> response = new HashMap<>();
        switch (targetDataBase) {
            case "ols":
                List<Map<String, Object>> transformedResults = originalResponse.stream()
                        .map(olsTransformer::transformItem)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                response = olsTransformer.constructResponse(transformedResults);
                break;
            case "bioportal":
                List<Map<String, Object>> transformedResultsBioportal = originalResponse.stream()
                        .map(bioportalTransformer::transformItem)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                response = bioportalTransformer.constructResponse(transformedResultsBioportal);
                break;
            default:
                response.put("error", "No database configuration found");
                break;
        }
        return response;
    }

    // Other methods and members
}
