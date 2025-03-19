package org.semantics.apigateway;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.mockito.Mock;
import org.semantics.apigateway.config.DatabaseConfig;
import org.semantics.apigateway.service.ApiAccessor;
import org.semantics.apigateway.service.configuration.ConfigurationLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Type;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

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

    /**
     * Create fixture for the AGROVOC ontoportal artefact
     */
    protected Map<String, Object> createOntoportalAgrovocFixture() {
        Map<String, Object> fixture = new HashMap<>();
        //TODO: add source_url
        //TODO: add @type
        //TODO: add @context
        //TODO: add @id
        //TODO: add other fields

        fixture.put("iri", "http://aims.fao.org/aos/agrovoc/");
        fixture.put("backend_type", "ontoportal");
        fixture.put("short_form", "AGROVOC");
        fixture.put("label", "AGROVOC");
        fixture.put("source", "https://data.agroportal.lirmm.fr");
        fixture.put("source_name", "agroportal");
        fixture.put("ontology", "AGROVOC");
        fixture.put("descriptions", List.of(
                "AGROVOC is a multilingual and controlled vocabulary designed to cover concepts and terminology under FAO's areas of interest. It is a large Linked Open Data set about agriculture, available for public use, and its highest impact is through facilitating the access and visibility of data across domains and languages."
        ));
        return fixture;
    }

    /**
     * Create fixture for the AGROVOC skosmos artefact
     */
    protected Map<String, Object> createSkosmosAgrovocFixture() {
        Map<String, Object> fixture = new HashMap<>();
        fixture.put("backend_type", "skosmos");
        fixture.put("label", "agrovoc");
        fixture.put("short_form", "agrovoc");
        fixture.put("source", "https://agrovoc.fao.org/browse/rest/v1");
        fixture.put("source_name", "agrovoc");
        fixture.put("ontology", "agrovoc");
        fixture.put("descriptions", List.of("AGROVOC Multilingual Thesaurus"));
        return fixture;
    }

    /**
     * Create fixture for the AGROVOC ols artefact
     */
    protected Map<String, Object> createOlsAgrovocFixture() {
        Map<String, Object> fixture = new HashMap<>();
        fixture.put("backend_type", "ols");
        fixture.put("label", "AGROVOC Multilingual Thesaurus");
        fixture.put("short_form", "agrovoc");
        fixture.put("source", "https://semanticlookup.zbmed.de/ols/api");
        fixture.put("source_name", "zbmed");
        fixture.put("ontology", "agrovoc");
        return fixture;
    }

    protected void mockApiAccessor(String key, ApiAccessor apiAccessor) {
        this.apiAccessor = apiAccessor;
        apiAccessor.setRestTemplate(restTemplate);
        this.configs = configurationLoader.getDatabaseConfigs();
        this.mockResponses = this.readMockedResponses(key, configs);
        when(restTemplate.getForEntity(
                anyString(),
                eq(Object.class)))
                .thenAnswer(invocation -> {
                    String url = invocation.getArgument(0, String.class);
                    ResponseEntity<Map<String, Object>> response = ResponseEntity.status(404).body(new HashMap<>());
                    for (DatabaseConfig config : configs) {
                        String configHost = new URL(config.getUrl()).getHost();
                        String currentURLHost = new URL(url).getHost();


                        if (configHost.equals(currentURLHost)) {
                            String jsonResponse = mockResponses.get(config.getName());
                            Gson gson = new Gson();
                            Type mapType = new TypeToken<Object>() {
                            }.getType();
                            Object map = gson.fromJson(jsonResponse, mapType);

                            if (map instanceof List) {
                                Map<String, Object> out = new HashMap<>();
                                out.put("collection", map);
                                response = ResponseEntity.status(200).body(out);
                            } else {
                                response = ResponseEntity.status(200).body((Map<String, Object>) map);
                            }


                            return response;
                        }
                    }
                    return response;
                });
    }


    protected Map<String, Object> findByIriAndBackendType(List<Map<String, Object>> responseList, String iri, String backendType) {
        return responseList.stream().filter(x -> x.get("iri").equals(iri) && x.get("backend_type").equals(backendType)).findFirst().orElse(null);
    }

    protected Map<String, Object> findByShortFormAndBackendType(List<Map<String, Object>> responseList, String shortForm, String backendType) {
        return responseList.stream().filter(x -> x.get("short_form").equals(shortForm) && x.get("backend_type").equals(backendType)).findFirst().orElse(null);
    }
}
