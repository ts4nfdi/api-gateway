package org.semantics.apigateway.service;

import lombok.Getter;
import org.semantics.apigateway.api.ModTransformer;
import org.semantics.apigateway.api.OlsTransformer;
import org.semantics.apigateway.api.OntoPortalTransformer;
import org.semantics.apigateway.api.SkosmosTransformer;
import org.semantics.apigateway.config.DatabaseConfig;
import org.semantics.apigateway.service.configuration.ConfigurationLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Getter
public class ResponseTransformerService {

    private final ConfigurationLoader configurationLoader;

    public ResponseTransformerService(ConfigurationLoader configurationLoader) {
        this.configurationLoader = configurationLoader;
    }


    // Method to transform and structure results based on database
    public Map<String, Object> transformAndStructureResults(List<Map<String, Object>> combinedResults, String
            targetDbSchema, String endpoint, Boolean isList) throws IOException {
        return transformJsonResponse(combinedResults, targetDbSchema, endpoint, isList);
    }

    // Method to transform the JSON response from a database into a specific format
    private Map<String, Object>  transformJsonResponse(List<Map<String, Object>> originalResponse, String targetDataBase, String endpoint, Boolean isList) {
        DatabaseConfig databaseConfig = configurationLoader.getDatabaseConfig(targetDataBase);
        switch (targetDataBase) {
            case "ols":
                OlsTransformer olsTransformer = new OlsTransformer();
                List<Map<String, Object>> transformedResults = originalResponse.stream()
                        .map(x ->  olsTransformer.transformItem(x, databaseConfig.getResponseMapping(endpoint)))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                return olsTransformer.constructResponse(transformedResults, false);
            case "ontoportal":
                OntoPortalTransformer ontoPortalTransformer = new OntoPortalTransformer();
                List<Map<String, Object>> transformedResultsOntoPortal = originalResponse.stream()
                        .map(x ->  ontoPortalTransformer.transformItem(x, databaseConfig.getResponseMapping(endpoint)))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                return ontoPortalTransformer.constructResponse(transformedResultsOntoPortal, false);
            case "skosmos":
                SkosmosTransformer skosmosTransformer = new SkosmosTransformer();
                List<Map<String, Object>> transformedResultsSkosmos = originalResponse.stream()
                        .map(x ->  skosmosTransformer.transformItem(x, databaseConfig.getResponseMapping(endpoint)))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                return  skosmosTransformer.constructResponse(transformedResultsSkosmos, false);
             case "mod":
                 ModTransformer modTransformer = new ModTransformer();
                    List<Map<String, Object>> transformedResultsMod = originalResponse.stream()
                            .map(x -> modTransformer.transformItem(x, databaseConfig.getResponseMapping(endpoint)))
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

                    return modTransformer.constructResponse(transformedResultsMod, isList);
            default:

                return (Map<String, Object>) new HashMap<>().put("error", "No database configuration found");
        }
    }
}
