package org.semantics.apigateway.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.semantics.apigateway.api.OlsTransformer;
import org.semantics.apigateway.api.OntoPortalTransformer;
import org.semantics.apigateway.api.SkosmosTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Getter
public class ResponseTransformerService {

    @Autowired
    private final ConfigurationLoader configurationLoader = new ConfigurationLoader();

    // Method to transform and structure results based on database
    public Map<String, Object> transformAndStructureResults(List<Map<String, Object>> combinedResults, String
            targetDbSchema) throws IOException {
        return transformJsonResponse(combinedResults, targetDbSchema);
    }

    // Method to transform the JSON response from a database into a specific format
    private Map<String, Object>  transformJsonResponse(List<Map<String, Object>> originalResponse, String targetDataBase) {
        switch (targetDataBase) {
            // Case for transforming data from the OLS database
            case "ols":
                OlsTransformer olsTransformer = new OlsTransformer();
                List<Map<String, Object>> transformedResults = originalResponse.stream()
                        .map(olsTransformer::transformItem)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                return olsTransformer.constructResponse(transformedResults);
            // Case for transforming data from the OntoPortal database
            case "ontoportal":
                OntoPortalTransformer ontoPortalTransformer = new OntoPortalTransformer();
                List<Map<String, Object>> transformedResultsOntoPortal = originalResponse.stream()
                        .map(ontoPortalTransformer::transformItem)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                return ontoPortalTransformer.constructResponse(transformedResultsOntoPortal);
            // Add more cases here for other databases as needed
            case "skosmos":
                SkosmosTransformer skosmosTransformer = new SkosmosTransformer();
                List<Map<String, Object>> transformedResultsSkosmos = originalResponse.stream()
                        .map(skosmosTransformer::transformItem)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                return  skosmosTransformer.constructResponse(transformedResultsSkosmos);
            default:
                return (Map<String, Object>) new HashMap<>().put("error", "No database configuration found");
        }
    }
}
