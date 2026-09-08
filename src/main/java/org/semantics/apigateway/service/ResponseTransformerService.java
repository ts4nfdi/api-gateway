package org.semantics.apigateway.service;

import lombok.Getter;
import org.semantics.apigateway.api.*;
import org.semantics.apigateway.config.DatabaseConfig;
import org.semantics.apigateway.config.ResponseMapping;
import org.semantics.apigateway.service.configuration.ConfigurationLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
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
          targetDbSchema, String endpoint, boolean isList, boolean paginate, int page, long totalCount,
          Map<String, SortedSet<String>> unsupportedParameters) {
    return transformJsonResponse(combinedResults, targetDbSchema, endpoint, isList, paginate, page, totalCount,  unsupportedParameters);
  }
  
  // Method to transform the JSON response from a database into a specific format
  private Map<String, Object> transformJsonResponse(List<Map<String, Object>> originalResponse, String targetDataBase,
                                                    String endpoint, boolean isList, boolean paginate, int page,
                                                    long totalCount, Map<String, SortedSet<String>> unsupportedParameters) {
    DatabaseConfig databaseConfig = configurationLoader.getDatabaseConfig(targetDataBase);
    
    DatabaseTransformer transformer = switch (targetDataBase) {
      case "ols" -> new OlsTransformer();
      case "ols2" -> new OlsV2Transformer();
      case "ontoportal" -> new OntoPortalTransformer();
      case "skosmos" -> new SkosmosTransformer();
      case "mod" -> new ModTransformer();
      default -> new DatabaseTransformer() {
        @Override
        public Map<String, Object> transformItem(Map<String, Object> item, ResponseMapping mapping) {
          return Map.of();
        }
        @Override
        public Map<String, Object> constructResponse(List<Map<String, Object>> transformedResults, String mappingKey, boolean list, boolean paginate, int page, long totalCount, Map<String, SortedSet<String>> unsupportedParameters) {
          return Map.of("error", "No transformer found for backend type " + targetDataBase);
        }
      };
    };
    
    var responseMapping = databaseConfig.getResponseMapping(endpoint);
    List<Map<String, Object>> transformedResults = originalResponse.stream()
            .map(x -> transformer.transformItem(x, responseMapping))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    
    return transformer.constructResponse(transformedResults, responseMapping.getKey(), isList, paginate, page, totalCount, unsupportedParameters);
  }
}
