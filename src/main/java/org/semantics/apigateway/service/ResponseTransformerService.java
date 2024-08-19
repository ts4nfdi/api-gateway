package org.semantics.apigateway.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.semantics.apigateway.config.DatabaseConfig;
import org.semantics.apigateway.config.OntologyConfig;
import org.semantics.apigateway.model.DynDatabaseTransform;
import org.semantics.apigateway.service.search.SearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Getter
public class ResponseTransformerService {

    @Autowired
    private final ConfigurationLoader configurationLoader = new ConfigurationLoader();

    private static final Logger logger = LoggerFactory.getLogger(ResponseTransformerService.class);

    // Method to transform and structure results based on database
    public Map<String, Object> transformAndStructureResults(List<Map<String, Object>> combinedResults, String
            targetDbSchema) throws IOException {
        Map<String, String> fieldMappings = loadFieldMappings(targetDbSchema);
        Map<String, Object> jsonSchema = loadJsonSchema(targetDbSchema);
        Map<String, String> responseMapping = configurationLoader.getResponseMappings().get(targetDbSchema);

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

        Map<String, Object> jsonConfig = objectMapper.readValue(inputStream, new TypeReference<Map<String, Object>>() {
        });
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
