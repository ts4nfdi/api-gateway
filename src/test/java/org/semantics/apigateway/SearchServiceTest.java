package org.semantics.apigateway;

import com.github.jsonldjava.utils.Obj;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.lucene.queryparser.classic.ParseException;
import org.hibernate.validator.internal.constraintvalidators.bv.AssertTrueValidator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.semantics.apigateway.config.OntologyConfig;
import org.semantics.apigateway.model.responses.AggregatedApiResponse;
import org.semantics.apigateway.service.ConfigurationLoader;
import org.semantics.apigateway.service.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SearchServiceTest {

    @Autowired
    private SearchService searchService;

    @Mock
    private RestTemplate restTemplate; // Mock RestTemplate

    @Autowired
    private ConfigurationLoader configurationLoader; // Autowire the real ConfigurationLoader


    @BeforeEach
    public void setup() {
        searchService.getAccessor().setRestTemplate(restTemplate);
        List<OntologyConfig> configs = configurationLoader.getOntologyConfigs();

        when(restTemplate.getForEntity(
                anyString(),
                any(Class.class)))
                .thenAnswer(invocation -> {
                    String url = invocation.getArgument(0, String.class);
                    ResponseEntity<Map<String, Object>> response = ResponseEntity.status(404).body(new HashMap<>());

                    for (OntologyConfig config : configs) {
                        String serviceName = String.format("src/test/resources/mocks/search/%s.json", config.getDatabase());
                        String jsonResponse = new String(Files.readAllBytes(Paths.get(serviceName)));

                        String configHost = new URL(config.getUrl()).getHost();
                        String currentURLHost = new URL(url).getHost();


                        if (configHost.equals(currentURLHost)) {

                            Gson gson = new Gson();
                            Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
                            Map<String, Object> map = gson.fromJson(jsonResponse, mapType);

                            response = ResponseEntity.status(200).body(map);
                            return response;
                        }
                    }
                    return response;
                });
    }

    @Test
    public void testSearchAllDatabases() {
        CompletableFuture<Object> r = searchService.performSearch("plant", "", "", "", false);

        AggregatedApiResponse response = (AggregatedApiResponse) r.join();

        List<Map<String, Object>> responseList = response.getCollection();
        
        assertThat(responseList).hasSize(100);

        Map<String, Object> firstPlant = responseList.get(0);
        assertThat(firstPlant.get("iri")).isEqualTo("http://sweetontology.net/matrPlant/Plant");
        assertThat(firstPlant.get("backend_type")).isEqualTo("ontoportal");
        assertThat(firstPlant.get("short_form")).isEqualTo("plant");
        assertThat(firstPlant.get("label")).isEqualTo("plant");
        assertThat(firstPlant.get("source")).isEqualTo("https://data.biodivportal.gfbio.dev");
        assertThat(firstPlant.get("type")).isEqualTo("class");
        assertThat(firstPlant.get("ontology")).isEqualTo("sweet");

        Map<String, Object> secondPlant = responseList.get(1);
        assertThat(secondPlant.get("iri")).isEqualTo("http://purl.obolibrary.org/obo/NCIT_C14258");
        assertThat(secondPlant.get("backend_type")).isEqualTo("ols");
        assertThat(secondPlant.get("short_form")).isEqualTo("NCIT_C14258");
        assertThat(secondPlant.get("description"))
                .isEqualTo(new ArrayList<String>(List.of(new String[]{"Any living organism that typically synthesizes its food from inorganic substances, possesses cellulose cell walls, responds slowly and often permanently to a stimulus, lacks specialized sense organs and nervous system, and has no powers of locomotion. (EPA Terminology Reference System)"})));
        assertThat(secondPlant.get("label")).isEqualTo("Plant");
        assertThat(secondPlant.get("source")).isEqualTo("https://ebi.ac.uk/ols4/api");
        assertThat(secondPlant.get("type")).isEqualTo("class");
        assertThat(secondPlant.get("ontology")).isEqualTo("ncit");


        assertThat(responseList.stream().map(x -> x.get("backend_type")).distinct().sorted().collect(Collectors.toList()))
                .isEqualTo(configurationLoader.getOntologyConfigs().stream().map(OntologyConfig::getDatabase).sorted().collect(Collectors.toList()));
    }

    @Test
    public void testSearchOlsSchema() throws IOException, ParseException {
        CompletableFuture<Object> r = searchService.performSearch("plant", "", "", "ols", false);

        Map<String, Object> response = (Map<String, Object>) r.join();

        assertThat(response.containsKey("response")).isTrue();
        assertThat(response.containsKey("responseHeader")).isTrue();


        List<Map<String, Object>> responseList = (List<Map<String, Object>>) ((Map<String, Object>) response.get("response")).get("docs");

        assertThat(responseList).hasSize(100);

    }

    @Test
    public void testSearchJsonLdFormat() {
        CompletableFuture<Object> r = searchService.performSearch("plant", "", "jsonld", "", false);

        List<Map<String, Object>> response = (List<Map<String, Object>>) r.join();

        Map<String, Object> firstPlant = response.get(0);
        assertThat(firstPlant.containsKey("@type")).isTrue();
        assertThat(firstPlant.containsKey("@context")).isTrue();
    }
}
