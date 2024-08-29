package org.semantics.apigateway.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.semantics.apigateway.config.DatabaseConfig;
import org.semantics.apigateway.config.DatabasesConfig;
import org.semantics.apigateway.config.ServiceConfig;
import org.semantics.apigateway.config.ServicesConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

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

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationLoader.class);
    private List<DatabaseConfig> databaseConfigs;
    private List<ServiceConfig> serviceConfigs;
    private Map<String, Map<String, String>> responseMappings;

    // Method invoked after on server start, loads database configurations and replace environment variables
    @PostConstruct
    public void loadDbConfigs() throws IOException {
        Resource servicesConfigResource = resourceLoader.getResource("classpath:services-config.json");
        Resource dataBasesConfigResource = resourceLoader.getResource("classpath:databases.json");


        String servicesConfigJson = StreamUtils.copyToString(servicesConfigResource.getInputStream(), StandardCharsets.UTF_8);

        String databaseConfigJson = StreamUtils.copyToString(dataBasesConfigResource.getInputStream(), StandardCharsets.UTF_8);

        for (Map.Entry<String, Object> property : environment.getSystemEnvironment().entrySet()) {
            String key = property.getKey();
            String value = (String) property.getValue();
            databaseConfigJson = databaseConfigJson.replace("${" + key + "}", value);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        DatabasesConfig dbConfig = objectMapper.readValue(databaseConfigJson, DatabasesConfig.class);
        ServicesConfig servicesConfig = objectMapper.readValue(servicesConfigJson, ServicesConfig.class);


        this.databaseConfigs = dbConfig.getDatabases();

        this.databaseConfigs.stream().forEach( x -> {
            x.setServiceConfig(servicesConfig.getService(x.getType()));
        });

        this.serviceConfigs = servicesConfig.getServices();
        this.responseMappings = loadResponseMappings();
        databaseConfigs.forEach(config -> logger.info("Loaded config: {}", config));
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

    public boolean databaseExist(String database) {
        return database == null || database.isEmpty() ||
                databaseConfigs.stream().anyMatch(config -> config.getDatabase().equalsIgnoreCase(database));
    }

    public DatabaseConfig getConfigByUrl(String url) {
        return databaseConfigs.stream()
                .filter(c -> c.getUrl().equalsIgnoreCase(url))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Config not found for URL: " + url));
    }

}
