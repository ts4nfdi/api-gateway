package org.semantics.apigateway;

import org.mockito.Mock;
import org.semantics.apigateway.config.DatabaseConfig;
import org.semantics.apigateway.service.ApiAccessor;
import org.semantics.apigateway.service.configuration.ConfigurationLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ApplicationTestAbstract {
    private final Logger logger = LoggerFactory.getLogger(SearchServiceTest.class);

    @Mock
    protected RestTemplate restTemplate; // Mock RestTemplate

    @Autowired
    protected ConfigurationLoader configurationLoader; // Autowire the real ConfigurationLoader

    protected ApiAccessor apiAccessor;

    protected Map<String, String> mockResponses = new HashMap<>();

    protected List<DatabaseConfig> configs;

    protected Map<String, String> readMockedResponses(String key, List<DatabaseConfig> configs) {
        Map<String, String> mockResponses = new HashMap<>();
        for (DatabaseConfig config : configs) {
            String serviceName = String.format("src/test/resources/mocks/"+key+"/%s.json", config.getName());
            String jsonResponse = "";
            try {
                jsonResponse = new String(Files.readAllBytes(Paths.get(serviceName)));
                mockResponses.put(config.getName(), jsonResponse);
                logger.info("Mocking file: {}", serviceName);
            } catch (Exception e) {
                logger.info("File: {} not found so not mocking", serviceName);
            }
        }
        return mockResponses;
    }

    protected void mockApiAccessor(String key, ApiAccessor apiAccessor) {
        this.apiAccessor = apiAccessor;
        apiAccessor.setRestTemplate(restTemplate);
        this.configs = configurationLoader.getDatabaseConfigs();
        this.mockResponses = this.readMockedResponses(key, configs);
    }


    protected Map<String, Object> findByIriAndBackendType(List<Map<String, Object>> responseList, String iri, String backendType) {
        return responseList.stream().filter(x -> x.get("iri").equals(iri) && x.get("backend_type").equals(backendType)).findFirst().orElse(null);
    }

    protected Map<String, Object> findByShortFormAndBackendType(List<Map<String, Object>> responseList, String shortForm, String backendType) {
        return responseList.stream().filter(x -> x.get("short_form").equals(shortForm) && x.get("backend_type").equals(backendType)).findFirst().orElse(null);
    }
}
