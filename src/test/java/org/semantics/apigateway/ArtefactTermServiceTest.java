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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Type;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ArtefactTermServiceTest {

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
                eq(Object.class)))
                .thenAnswer(invocation -> {
                    String url = invocation.getArgument(0, String.class);
                    ResponseEntity<Map<String, Object>> response = ResponseEntity.status(404).body(new HashMap<>());
                    for (DatabaseConfig config : configs) {
                        String serviceName = String.format("src/test/resources/mocks/artefact_term/%s.json", config.getName());
                        String jsonResponse = "";
                        try {
                            jsonResponse = new String(Files.readAllBytes(Paths.get(serviceName)));
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }

                        String configHost = new URL(config.getUrl()).getHost();
                        String currentURLHost = new URL(url).getHost();


                        if (configHost.equals(currentURLHost)) {

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

    @Test
    public void testGetArtefactTerm() {
        CompletableFuture<Object> r = artefactsService.getArtefactTerm("AGROVOC", "http://aims.fao.org/aos/agrovoc/c_330834", null, null, false);

        AggregatedApiResponse response = (AggregatedApiResponse) r.join();

        List<Map<String, Object>> responseList = response.getCollection();

        assertThat(responseList).hasSize(2);

    Map<String, Object> skosmosItem = responseList.get(0);
    assertThat(skosmosItem.get("iri")).isEqualTo("http://aims.fao.org/aos/agrovoc/c_330834");
    assertThat(skosmosItem.get("backend_type")).isEqualTo("skosmos");
    assertThat(skosmosItem.get("short_form")).isEqualTo("c_330834");
    assertThat(skosmosItem.get("label")).isEqualTo("activities");
    assertThat(skosmosItem.get("source")).isEqualTo("https://agrovoc.fao.org/browse/rest/v1");
    assertThat(skosmosItem.get("source_name")).isEqualTo("agrovoc");

    Map<String, Object> ontoportalItem = responseList.get(1);
    assertThat(ontoportalItem.get("iri")).isEqualTo("http://aims.fao.org/aos/agrovoc/c_330834");
    assertThat(ontoportalItem.get("backend_type")).isEqualTo("ontoportal");
    assertThat(ontoportalItem.get("short_form")).isEqualTo("c_330834");
    assertThat(ontoportalItem.get("label")).isEqualTo("activities");
    assertThat(ontoportalItem.get("source")).isEqualTo("https://data.agroportal.lirmm.fr");
    assertThat(ontoportalItem.get("type")).isEqualTo("class");
    assertThat(ontoportalItem.get("source_name")).isEqualTo("agroportal");
    assertThat(ontoportalItem.get("ontology")).isEqualTo("agrovoc");

    List<String> descriptionList = (List<String>) ontoportalItem.get("description");
    assertThat(descriptionList).hasSize(2);
    assertThat(descriptionList).containsExactlyInAnyOrder(
            "http://aims.fao.org/aos/agrovoc/xDef_d8a81e42",
            "http://aims.fao.org/aos/agrovoc/xDef_47a14ae7"
    );

    List<?> synonymList = (List<?>) ontoportalItem.get("synonym");
    assertThat(synonymList).isEmpty();  // Check if synonym list is empty

    assertThat(responseList.stream().map(x -> x.get("source_name")).distinct().sorted().toArray())
            .isEqualTo(new String[] {"agroportal", "agrovoc"});
    }

}