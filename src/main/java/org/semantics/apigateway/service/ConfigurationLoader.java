package org.semantics.apigateway.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.semantics.apigateway.config.DatabaseConfig;
import org.semantics.apigateway.config.OntologyConfig;
import org.semantics.apigateway.model.DynTransformResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Getter
public class ConfigurationLoader {

    @Autowired
    private ConfigurableEnvironment environment;

    @Autowired
    private ResourceLoader resourceLoader;

    private static final Logger logger = LoggerFactory.getLogger(DynSearchService.class);
    private List<OntologyConfig> ontologyConfigs;
    private Map<String, Map<String, String>> responseMappings;

    // Method invoked after on server start, loads database configurations and replace environment variables
    @PostConstruct
    public void loadDbConfigs() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:response-config.json");
        String jsonContent = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

        for (Map.Entry<String, Object> property : environment.getSystemEnvironment().entrySet()) {
            String key = property.getKey();
            String value = (String) property.getValue();
            jsonContent = jsonContent.replace("${" + key + "}", value);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        DatabaseConfig dbConfig = objectMapper.readValue(jsonContent, DatabaseConfig.class);
        this.ontologyConfigs = dbConfig.getDatabases();
        this.responseMappings = loadResponseMappings();
        ontologyConfigs.forEach(config -> logger.info("Loaded config: {}", config));
    }

    // Loads response mappings from a json configuration file
    private Map<String, Map<String, String>> loadResponseMappings() throws IOException {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("response-mappings.json");
        if (inputStream != null) {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Map<String, String>> mappings = objectMapper.readValue(inputStream, new TypeReference<Map<String, Map<String, String>>>() {
            });
            if (mappings != null) {
                return mappings;
            }
        }
        return Collections.emptyMap();
    }

}
