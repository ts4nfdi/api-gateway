package org.semantics.nfdi.service;

import org.semantics.nfdi.model.DynDatabaseTransform;
import org.semantics.nfdi.model.DynTransformResponse;
import org.semantics.nfdi.model.JsonLdTransform;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;

import org.semantics.nfdi.config.DatabaseConfig;
import org.semantics.nfdi.config.OntologyConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DynSearchService {

    @Value("classpath:response-config.yaml")
    private Resource dbConfigResource;
    private static final Logger logger = LoggerFactory.getLogger(DynSearchService.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final DynTransformResponse dynTransformResponse = new DynTransformResponse();
    private List<OntologyConfig> ontologyConfigs;

    private Map<String, Map<String, String>> responseMappings; // Add this field


    @PostConstruct
    public void loadDbConfigs() throws IOException {
        Yaml yaml = new Yaml(new Constructor(DatabaseConfig.class));
        try (InputStream in = dbConfigResource.getInputStream()) {
            DatabaseConfig dbConfig = yaml.loadAs(in, DatabaseConfig.class);
            this.ontologyConfigs = dbConfig.getDatabases();
            this.responseMappings = loadResponseMappings(); // Load response mappings
            ontologyConfigs.forEach(config -> logger.info("Loaded config: {}", config));
        }
    }

    private Map<String, Map<String, String>> loadResponseMappings() throws IOException {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("response-mappings.yaml");
        if (inputStream != null) {
            Yaml yaml = new Yaml();
            Map<String, Map<String, String>> mappings = yaml.load(inputStream);
            if (mappings != null) {
                return mappings;
            }
        }
        return Collections.emptyMap();
    }

    private String constructUrl(String query, OntologyConfig config) {
        String url = config.getUrl();
        String apiKey = config.getApiKey();
        return apiKey.isEmpty() ? String.format(url, query) : String.format(url, query, apiKey);
    }

    public List<Map<String, Object>> filterResultsByFacets(List<Map<String, Object>> results, Map<String, String> selectedFacets) {
        return results.stream()
                .filter(result -> selectedFacets.entrySet().stream()
                        .allMatch(facet -> result.containsKey(facet.getKey()) && result.get(facet.getKey()).equals(facet.getValue())))
                .collect(Collectors.toList());
    }

    @Async
    public CompletableFuture<List<Map<String, Object>>> search(String query, OntologyConfig config, String format) {
        CompletableFuture<List<Map<String, Object>>> future = new CompletableFuture<>();
        try {
            String url = constructUrl(query, config);
            logger.info("Accessing URL: {}", url);
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                logger.info("Raw API Response: {}", response.getBody());
                List<Map<String, Object>> transformedResponse = dynTransformResponse.dynTransformResponse(response.getBody(), config);

                if ("jsonld".equalsIgnoreCase(format)) {
                    transformedResponse = JsonLdTransform.convertToJsonLd(transformedResponse, config);
                }

                logger.info("Transformed API Response: {}", transformedResponse);
                future.complete(transformedResponse);
            } else {
                logger.error("API Response Error: Status Code - {}", response.getStatusCode());
                future.complete(List.of());
            }
        } catch (Exception e) {
            logger.error("An error occurred while processing the request: {}", e.getMessage(), e);
            future.completeExceptionally(e);
        }
        return future;
    }

    public CompletableFuture<Object> performDynFederatedSearch(
            String query, String database, String format, String targetDbSchema) {
        CompletableFuture<Object> future = new CompletableFuture<>();

        boolean databaseExists = database != null && !database.isEmpty() &&
                ontologyConfigs.stream().anyMatch(config -> config.getDatabase().equalsIgnoreCase(database));

        if (!databaseExists && database != null && !database.isEmpty()) {
            future.completeExceptionally(new IllegalArgumentException("Database not found: " + database));
            return future;
        }

        Stream<OntologyConfig> configsStream = ontologyConfigs.stream();
        if (database != null && !database.isEmpty()) {
            configsStream = configsStream.filter(config -> database.equalsIgnoreCase(config.getDatabase()));
        }

        List<CompletableFuture<List<Map<String, Object>>>> futures = configsStream
                .map(config -> search(query, config, format))
                .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    try {
                        List<Map<String, Object>> combinedResults = futures.stream()
                                .flatMap(resultFuture -> resultFuture.join().stream())
                                .collect(Collectors.toList());

                        logger.info("Combined results before transformation: {}", combinedResults);

                        if (targetDbSchema != null && !targetDbSchema.isEmpty()) {
                            Object transformedResults = transformAndStructureResults(combinedResults, targetDbSchema);
                            logger.info("Transformed results for database schema: {}", transformedResults);
                            return transformedResults;
                        } else {
                            return combinedResults;
                        }
                    } catch (Exception e) {
                        logger.error("Error in transforming results for database schema: {}", e.getMessage(), e);
                        future.completeExceptionally(e);
                        return null;
                    }
                });
    }

    // Method to transform and structure results based on database
    private Object transformAndStructureResults(List<Map<String, Object>> combinedResults, String targetDbSchema) {
        OntologyConfig config = ontologyConfigs.stream()
                .filter(c -> c.getDatabase().equalsIgnoreCase(targetDbSchema))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Database config not found"));

        Map<String, String> fieldMappings = loadFieldMappings(targetDbSchema);
        Map<String, Object> jsonSchema = loadJsonSchema(targetDbSchema);
        Map<String, String> responseMapping = responseMappings.get(targetDbSchema); // Get response mapping

        DynDatabaseTransform dynDatabaseTransform = new DynDatabaseTransform(fieldMappings, jsonSchema, responseMapping);
        return dynDatabaseTransform.transformDatabaseResponse(combinedResults, targetDbSchema);
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

    private Map<String, String> loadFieldMappings(String targetDbSchema) {
        Map<String, String> fieldMappings = new HashMap<>();

        InputStream inputStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream("db-schema-config.yaml");
        if (inputStream == null) {
            throw new RuntimeException("Failed to load YAML file for field mappings");
        }

        Yaml yaml = new Yaml();
        Map<String, Object> yamlConfig = yaml.load(inputStream);

        if (yamlConfig != null && yamlConfig.containsKey("dbSchema")) {
            Map<String, Object> dbSchema = (Map<String, Object>) yamlConfig.get("dbSchema");
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
