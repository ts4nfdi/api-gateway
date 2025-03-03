package org.semantics.apigateway.service.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import jakarta.annotation.PostConstruct;
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
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
    public void loadDbConfigs() {
        try {
            this.serviceConfigs = loadServiceConfigurations();
            this.databaseConfigs = loadDatabaseConfigurations();

            for (DatabaseConfig dbConfig : databaseConfigs) {
                ServicesConfig tempConfig = new ServicesConfig(serviceConfigs);
                dbConfig.setServiceConfig(tempConfig.getService(dbConfig.getType()));
            }
            databaseConfigs.forEach(config -> logger.info("Loaded config: {}", config));
        } catch (IOException e) {
            logger.error("Failed to load configurations", e);
            throw new RuntimeException("Error loading configuration files", e);
        }
    }


    private List<ServiceConfig> loadServiceConfigurations() throws IOException {
        List<ServiceConfig> services = new ArrayList<>();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] yamlResources = resolver.getResources("classpath:/backend_types/*.yml");

        if (yamlResources.length == 0) {
            logger.warn("No YAML files found in backend_types directory");
            return services;
        }

        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

        for (Resource resource : yamlResources) {
            try (InputStream inputStream = resource.getInputStream()) {
                logger.info("Loading service configuration from: {}", resource.getFilename());
                ServiceConfig serviceConfig = objectMapper.readValue(inputStream, ServiceConfig.class);
                services.add(serviceConfig);
            } catch (Exception e) {
                logger.error("Error loading service configuration from file: {}", resource.getFilename(), e);
                throw new RuntimeException("Error loading service configuration", e);
            }
        }

        logger.info("Loaded {} service configurations from backend_types directory", services.size());
        return services;
    }


    private List<DatabaseConfig> loadDatabaseConfigurations() throws IOException {
        Resource dataBasesConfigResource = resourceLoader.getResource("classpath:databases.json");
        String databaseConfigJson = StreamUtils.copyToString(dataBasesConfigResource.getInputStream(), StandardCharsets.UTF_8);

        // Replace environment variables in the config
        for (Map.Entry<String, Object> property : environment.getSystemEnvironment().entrySet()) {
            String key = property.getKey();
            String value = String.valueOf(property.getValue());
            databaseConfigJson = databaseConfigJson.replace("${" + key + "}", value);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        DatabasesConfig dbConfig = objectMapper.readValue(databaseConfigJson, DatabasesConfig.class);

        return dbConfig.getDatabases();
    }

    public boolean databaseExist(String database) {
        return database == null || database.isEmpty() ||
                databaseConfigs.stream().anyMatch(config -> config.getName().equalsIgnoreCase(database));
    }

    public DatabaseConfig getDatabaseConfig(String database) {
        return databaseConfigs.stream()
                .filter(c -> c.getDatabase().equalsIgnoreCase(database))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Config not found for database: " + database));
    }

    public DatabaseConfig getConfigByUrl(String url, String endpoint) {
        System.out.println(databaseConfigs.get(0).getDatabase());

        return databaseConfigs.stream()
                .filter(c -> c.getUrl(endpoint).equalsIgnoreCase(url))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Config not found for URL: " + url));
    }

}
