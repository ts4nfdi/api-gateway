package org.semantics.apigateway;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.semantics.apigateway.config.DatabaseConfig;
import org.semantics.apigateway.model.responses.AggregatedApiResponse;
import org.semantics.apigateway.service.ArtefactsService;
import org.semantics.apigateway.service.ConfigurationLoader;
import org.semantics.apigateway.service.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Type;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ArtefactsServiceTest {

    @Autowired
    private ArtefactsService artefactsService;

    @Mock
    private RestTemplate restTemplate; // Mock RestTemplate

    @Autowired
    private ConfigurationLoader configurationLoader; // Autowire the real ConfigurationLoader


    @BeforeEach
    public void setup() {
        artefactsService.getAccessor().setRestTemplate(restTemplate);
        List<DatabaseConfig> configs = configurationLoader.getDatabaseConfigs();

        when(restTemplate.getForEntity(
                anyString(),
                any(Class.class)))
                .thenAnswer(invocation -> {
                    String url = invocation.getArgument(0, String.class);
                    ResponseEntity<Map<String, Object>> response = ResponseEntity.status(404).body(new HashMap<>());
                    for (DatabaseConfig config : configs) {
                        String serviceName = String.format("src/test/resources/mocks/artefacts/%s.json", config.getName());
                        String jsonResponse = "";
                        try {
                           jsonResponse = new String(Files.readAllBytes(Paths.get(serviceName)));
                        } catch (Exception e){
                            System.out.println(e.getMessage());
                        }

                        String configHost = new URL(config.getUrl()).getHost();
                        String currentURLHost = new URL(url).getHost();


                        if (configHost.equals(currentURLHost)) {

                            Gson gson = new Gson();
                            Type mapType = new TypeToken<Object>() {}.getType();
                            Object map = gson.fromJson(jsonResponse, mapType);

                            if(map instanceof List){
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

    @Test
    public void testGetAllArtefacts() {
        CompletableFuture<Object> r = artefactsService.getArtefacts("",  null, null, false);

        AggregatedApiResponse response = (AggregatedApiResponse) r.join();

        List<Map<String, Object>> responseList = response.getCollection();
        
        assertThat(responseList).hasSize(216);

        Map<String, Object> ontoportalItem = responseList.get(0);
        assertThat(ontoportalItem.get("iri")).isEqualTo("https://data.agroportal.lirmm.fr/ontologies/AAO");
        assertThat(ontoportalItem.get("backend_type")).isEqualTo("ontoportal");
        assertThat(ontoportalItem.get("short_form")).isEqualTo("aao");
        assertThat(ontoportalItem.get("label")).isEqualTo("AAO");
        assertThat(ontoportalItem.get("source")).isEqualTo("https://data.agroportal.lirmm.fr");
        assertThat(ontoportalItem.get("type")).isEqualTo("class");
        assertThat(ontoportalItem.get("source_name")).isEqualTo("agroportal");
        assertThat(ontoportalItem.get("ontology")).isEqualTo("AAO");

        List<String> descriptionList = (List<String>) ontoportalItem.get("description");
        assertThat(descriptionList).hasSize(1);
        assertThat(descriptionList.get(0)).isEqualTo("Agriculture Activity Ontology");

        Map<String, Object> skosmosItem = responseList.get(198);

        assertThat(skosmosItem.get("backend_type")).isEqualTo("skosmos");
        assertThat(skosmosItem.get("label")).isEqualTo("agrovoc");
        assertThat(skosmosItem.get("source")).isEqualTo("https://agrovoc.fao.org/browse/rest/v1");
        assertThat(skosmosItem.get("source_name")).isEqualTo("agrovoc");


        List<String> descriptionList2 = (List<String>) skosmosItem.get("description");
        assertThat(descriptionList2).hasSize(1);
        assertThat(descriptionList2.get(0)).isEqualTo("AGROVOC Multilingual Thesaurus");


        Map<String, Object> olsItem = responseList.get(207);

        assertThat(olsItem.get("backend_type")).isEqualTo("ols");
        assertThat(olsItem.get("label")).isEqualTo("bto");
        assertThat(olsItem.get("source")).isEqualTo("https://ebi.ac.uk/ols4/api");
        assertThat(olsItem.get("source_name")).isEqualTo("ols-ebi");


        assertThat(responseList.stream().map(x -> x.get("backend_type")).distinct().sorted().collect(Collectors.toList()))
                .isEqualTo(configurationLoader.getDatabaseConfigs().stream().map(DatabaseConfig::getDatabase).sorted().distinct().collect(Collectors.toList()));
        assertThat(responseList.stream().map(x -> x.get("source_name")).distinct().sorted().toArray())
                .isEqualTo(new String[] {"agroportal", "agrovoc", "ols-ebi"});
    }

}
