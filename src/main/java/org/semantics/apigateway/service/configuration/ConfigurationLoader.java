package org.semantics.apigateway.service.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.semantics.apigateway.config.SourceConfig;
import org.semantics.apigateway.config.SourcesConfig;
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
    private List<SourceConfig> sourceConfigs;
    private List<ServiceConfig> serviceConfigs;

    public  ConfigurationLoader(ResourceLoader resourceLoader, ConfigurableEnvironment environment) {
        this.resourceLoader = resourceLoader;
        this.environment = environment;
    }

    // Method invoked after on server start, loads source configurations and replace environment variables
    @PostConstruct
    public void loadDbConfigs() {
        try {
            this.serviceConfigs = loadServiceConfigurations();
            this.sourceConfigs = loadSourceConfigurations();

            for (SourceConfig dbConfig : sourceConfigs) {
                ServicesConfig tempConfig = new ServicesConfig(serviceConfigs);
                dbConfig.setServiceConfig(tempConfig.getService(dbConfig.getType()));
            }
            sourceConfigs.forEach(config -> logger.info("Loaded config: {}", config));
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


    private List<SourceConfig> loadSourceConfigurations() throws IOException {
        Resource sourcesConfigResource = resourceLoader.getResource("classpath:sources.json");
        String sourcesConfigJson = StreamUtils.copyToString(sourcesConfigResource.getInputStream(), StandardCharsets.UTF_8);

        // Replace environment variables in the config
        for (Map.Entry<String, Object> property : environment.getSystemEnvironment().entrySet()) {
            String key = property.getKey();
            String value = String.valueOf(property.getValue());
            sourcesConfigJson = sourcesConfigJson.replace("${" + key + "}", value);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        SourcesConfig sourcesConfig = objectMapper.readValue(sourcesConfigJson, SourcesConfig.class);

        return sourcesConfig.getSources();
    }

    public SourceConfig getSourceConfig(String source) {
        return serviceConfigs.stream()
                .filter(c -> c.getName().equalsIgnoreCase(source))
                .map(ServiceConfig::getSourceConfig)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Config not found for source: " + source));
    }

    public SourceConfig getConfigByUrl(String url, String endpoint) {
        return sourceConfigs.stream()
                .filter(c -> c.getUrl(endpoint).equalsIgnoreCase(url))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Config not found for URL: " + url));
    }

    public SourceConfig getConfigByName(String sourceName){
        return sourceConfigs.stream()
                .filter(c -> c.getName().equalsIgnoreCase(sourceName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Config not found for source name: " + sourceName));
    }

    public SourceConfig getConfigByBaseUrl(String url) {
        return sourceConfigs.stream()
                .filter(c -> {
                    try {
                        URL configUrl = new URL(c.getUrl());
                        return configUrl.getHost().equalsIgnoreCase(new URL(url).getHost());
                    } catch (Exception e) {
                        logger.error("Error parsing URL: {}", e.getMessage(), e);
                        return false;
                    }
                })
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Config not found for URL: " + url));
    }

}
