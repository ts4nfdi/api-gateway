package org.semantics.apigateway.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.semantics.apigateway.config.DatabaseConfig;
import org.semantics.apigateway.config.OntologyConfig;
import org.semantics.apigateway.model.DynDatabaseTransform;
import org.semantics.apigateway.model.DynTransformResponse;
import org.semantics.apigateway.utils.ApiAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SearchService {

    // Resource path to the JSON configuration file for database configurations
    @Value("classpath:response-config.json")
    private Resource dbConfigResource;
    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);
    private final DynTransformResponse dynTransformResponse = new DynTransformResponse();
    private List<OntologyConfig> ontologyConfigs;
    private Map<String, Map<String, String>> responseMappings;

    // Method invoked after the bean’s properties have been set, loads database configurations
    @PostConstruct
    public void loadDbConfigs() throws IOException {
     ObjectMapper objectMapper = new ObjectMapper();
        try (InputStream in = dbConfigResource.getInputStream()) {
            DatabaseConfig dbConfig = objectMapper.readValue(in, DatabaseConfig.class);
            this.ontologyConfigs = dbConfig.getDatabases();
            this.responseMappings = loadResponseMappings();
            ontologyConfigs.forEach(config -> logger.info("Loaded config: {}", config));
        }
    }


    private boolean databaseExist(String database){
        return database != null && !database.isEmpty() &&
                ontologyConfigs.stream().anyMatch(config -> config.getDatabase().equalsIgnoreCase(database));
    }

    // Performs a federated search across multiple databases and optionally transforms the results for a target database schema
    public CompletableFuture<List<Map<String, Object>>> performSearch(
            String query, String database, String format, String targetDbSchema) {
        CompletableFuture<List<Map<String, Object>>> future = new CompletableFuture<>();

        if(!databaseExist(database)){
            future.completeExceptionally(new IllegalArgumentException("Database not found: " + database));
            return future;
        }

//        Stream<OntologyConfig> configsStream = ontologyConfigs.stream();
//        if (database != null && !database.isEmpty()) {
//            configsStream = configsStream.filter(config -> database.equalsIgnoreCase(config.getDatabase()));
//        }


        Map<String, String> apis = ontologyConfigs.stream()
                .collect(Collectors.toMap(OntologyConfig::getUrl, OntologyConfig::getApiKey));

        ApiAccessor accessor = new ApiAccessor(apis, logger);

        CompletableFuture<Map<String, Map<String, Object>>> originalData = accessor.get(query);


        List<List<Map<String, Object>>> transformedData = originalData.thenApply(data ->
                data.entrySet().stream()
                        .map(entry -> {
                            String url = entry.getKey();
                            Map<String, Object> results = entry.getValue();
                            OntologyConfig config = ontologyConfigs.stream()
                                    .filter(c -> c.getUrl().equalsIgnoreCase(url))
                                    .findFirst()
                                    .orElseThrow(() -> new RuntimeException("Config not found for URL: " + url));
                            return dynTransformResponse.dynTransformResponse(results, config);
                        })
                        .collect(Collectors.toList())
        ).join();

        future.complete(transformedData.stream()
                .flatMap(List::stream)
                .collect(Collectors.toList()));


        return future;

//        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
//                .thenApply(v -> {
//                    try {
//                        List<Map<String, Object>> combinedResults = futures.stream()
//                                .flatMap(resultFuture -> resultFuture.join().stream())
//                                .collect(Collectors.toList());
//
//                        logger.info("Combined results before transformation: {}", combinedResults);
//
//                        if (targetDbSchema != null && !targetDbSchema.isEmpty()) {
//                            Object transformedResults = transformAndStructureResults(combinedResults, targetDbSchema);
//                            logger.info("Transformed results for database schema: {}", transformedResults);
//                            return transformedResults;
//                        } else {
//                            return combinedResults;
//                        }
//                    } catch (Exception e) {
//                        logger.error("Error in transforming results for database schema: {}", e.getMessage(), e);
//                        future.completeExceptionally(e);
//                        return null;
//                    }
//                });
    }


    // Loads response mappings from a json configuration file
    private Map<String, Map<String, String>> loadResponseMappings() throws IOException {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("response-mappings.json");
        if (inputStream != null) {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Map<String, String>> mappings = objectMapper.readValue(inputStream, new TypeReference<Map<String, Map<String, String>>>(){});
            if (mappings != null) {
                return mappings;
            }
        }
        return Collections.emptyMap();
    }

    // Method to transform and structure results based on database
    private Object transformAndStructureResults(List<Map<String, Object>> combinedResults, String targetDbSchema) throws IOException {
        OntologyConfig config = ontologyConfigs.stream()
                .filter(c -> c.getDatabase().equalsIgnoreCase(targetDbSchema))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Database config not found"));

        Map<String, String> fieldMappings = loadFieldMappings(targetDbSchema);
        Map<String, Object> jsonSchema = loadJsonSchema(targetDbSchema);
        Map<String, String> responseMapping = responseMappings.get(targetDbSchema);

        DynDatabaseTransform dynDatabaseTransform = new DynDatabaseTransform(fieldMappings, jsonSchema, responseMapping);
        return dynDatabaseTransform.transformJsonResponse(combinedResults, targetDbSchema);
    }

    private Map<String, Object> loadJsonSchema(String targetDbSchema) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            InputStream inputStream = new ClassPathResource(targetDbSchema + ".json").getInputStream();
            return objectMapper.readValue(inputStream, Map.class);
        } catch (IOException e) {
            logger.error("Error reading JSON schema file: " + targetDbSchema, e);
            return null;
        }
    }

    // Method to load field mappings from a JSON configuration file
    private Map<String, String> loadFieldMappings(String targetDbSchema) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        InputStream inputStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream("db-schema-config.json");
        if (inputStream == null) {
            throw new RuntimeException("Failed to load JSON file for field mappings");
        }

        Map<String, Object> jsonConfig = objectMapper.readValue(inputStream, new TypeReference<Map<String, Object>>(){});
        Map<String, String> fieldMappings = new HashMap<>();

        if (jsonConfig != null && jsonConfig.containsKey("dbSchema")) {
            Map<String, Object> dbSchema = (Map<String, Object>) jsonConfig.get("dbSchema");
            if (dbSchema != null && dbSchema.containsKey(targetDbSchema)) {
                Map<String, Object> schemaConfig = (Map<String, Object>) dbSchema.get(targetDbSchema);
                if (schemaConfig != null && schemaConfig.containsKey("mapping")) {
                    Map<String, String> schemaMapping = (Map<String, String>) schemaConfig.get("mapping");
                    fieldMappings.putAll(schemaMapping);
                }
            }
        }

        return fieldMappings;
    }
}
