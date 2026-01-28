package org.semantics.apigateway.service.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.semantics.apigateway.config.DatabaseConfig;
import org.semantics.apigateway.config.DatabasesConfig;
import org.semantics.apigateway.config.ServiceConfig;
import org.semantics.apigateway.config.ServicesConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Getter
public class ConfigurationLoader {

    private final ConfigurableEnvironment environment;
    private final ResourceLoader resourceLoader;

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationLoader.class);
    private List<DatabaseConfig> databaseConfigs;
    private List<ServiceConfig> serviceConfigs;

    public  ConfigurationLoader(ResourceLoader resourceLoader, ConfigurableEnvironment environment) {
        this.resourceLoader = resourceLoader;
        this.environment = environment;
    }

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
        ObjectMapper objectMapper = new ObjectMapper();
        for (Resource resource : yamlResources) {
            try (InputStream inputStream = resource.getInputStream()) {
                Yaml yaml = new Yaml();
                Map<String, Object> rawYamlData = yaml.load(inputStream);
                logger.info("Loading service configuration from: {}", resource.getFilename());
                ServiceConfig serviceConfig = objectMapper.convertValue(rawYamlData, ServiceConfig.class);
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

    public DatabaseConfig getDatabaseConfig(String database) {
        return serviceConfigs.stream()
                .filter(c -> c.getName().equalsIgnoreCase(database))
                .map(ServiceConfig::getDatabaseConfig)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Config not found for database: " + database));
    }

    public DatabaseConfig getConfigByUrl(String url, String endpoint) {
        return databaseConfigs.stream()
                .filter(c -> c.getUrl(endpoint).equalsIgnoreCase(url))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Config not found for URL: " + url));
    }

    public DatabaseConfig getConfigByName(String sourceName){
        return databaseConfigs.stream()
                .filter(c -> c.getName().equalsIgnoreCase(sourceName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Config not found for source name: " + sourceName));
    }

    public DatabaseConfig getConfigByBaseUrl(String url) {
        return databaseConfigs.stream()
                .filter(c -> {
                    try {
                        String configUrl = c.getUrl();
                        String type = c.getType();
                        String browserUrl;
                        switch (type) {
                            case "skosmos":

                                browserUrl= url.substring(0,(url.lastIndexOf("/v1/") + 3));
                                if (configUrl.equalsIgnoreCase(browserUrl)) {
                                    return true;
                                }

                            case "ols":
                            case "jskos2":

                                browserUrl = url.substring(0,(url.lastIndexOf("/api/") + 4));
                                if (configUrl.equalsIgnoreCase(browserUrl)) {
                                    return true;
                                }

                            case "ols2":
                                browserUrl = url.substring(0,( url.lastIndexOf("/api/v2") + 7));
                                if (configUrl.equalsIgnoreCase(browserUrl)) {
                                    return true;
                                }

                            default:
                                URL baseUrl = new URL(url);
                                browserUrl = baseUrl.getProtocol() + "://" + baseUrl.getHost();
                                return configUrl.equalsIgnoreCase(browserUrl);
                        }

                    } catch (Exception e) {
                        logger.error("Error parsing URL: {}", e.getMessage(), e);
                        return false;
                    }
                })
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Config not found for URL: " + url));
    }

}
