package org.semantics.apigateway;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.semantics.apigateway.config.DatabaseConfig;
import org.semantics.apigateway.model.CommonRequestParams;
import org.semantics.apigateway.model.ResponseFormat;
import org.semantics.apigateway.model.TargetDbSchema;
import org.semantics.apigateway.model.responses.AggregatedApiResponse;
import org.semantics.apigateway.service.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SearchServiceTest extends ApplicationTestAbstract {

    @Autowired
    private SearchService searchService;


    @BeforeEach
    public void setup() {
        mockApiAccessor("search", searchService.getAccessor());
        when(restTemplate.getForEntity(
                anyString(),
                eq(Object.class)))
                .thenAnswer(invocation -> {
                    String url = invocation.getArgument(0, String.class);
                    ResponseEntity<Map<String, Object>> response = ResponseEntity.status(404).body(new HashMap<>());
                    for (DatabaseConfig config : configs) {
                        String configHost = new URL(config.getUrl()).getHost();
                        String currentURLHost = new URL(url).getHost();
                        String jsonResponse = mockResponses.get(config.getName());

                        if (configHost.equals(currentURLHost)) {

                            Gson gson = new Gson();
                            Type mapType = new TypeToken<Map<String, Object>>() {
                            }.getType();
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
        CommonRequestParams commonRequestParams = new CommonRequestParams();
        CompletableFuture<Object> r = searchService.performSearch("plant", commonRequestParams, null, null, null, apiAccessor);

        AggregatedApiResponse response = (AggregatedApiResponse) r.join();

        List<Map<String, Object>> responseList = response.getCollection();

        assertThat(responseList).hasSize(100);

        responseList.stream().allMatch(x -> x.containsKey("label") && x.get("label").toString().toLowerCase().startsWith("plant"));
        //TODO need a better testing of the ranking
        Map<String, Object> firstPlant = findByIriAndBackendType(responseList, "http://sweetontology.net/matrPlant/Plant", "ontoportal");
        assertThat(firstPlant.get("iri")).isEqualTo("http://sweetontology.net/matrPlant/Plant");
        assertThat(firstPlant.get("backend_type")).isEqualTo("ontoportal");
//        assertThat(firstPlant.get("short_form")).isEqualTo("plant"); // TODO implement default value logic
        assertThat(firstPlant.get("label")).isEqualTo("plant");
        assertThat(firstPlant.get("source")).isEqualTo("https://data.biodivportal.gfbio.org");
//        assertThat(firstPlant.get("type")).isEqualTo("class"); // TODO implement default value logic
//        assertThat(firstPlant.get("ontology")).isEqualTo("sweet"); // TODO implement default value logic

        Map<String, Object> secondPlant = findByIriAndBackendType(responseList, "http://purl.obolibrary.org/obo/NCIT_C14258", "ols");
        assertThat(secondPlant.get("iri")).isEqualTo("http://purl.obolibrary.org/obo/NCIT_C14258");
        assertThat(secondPlant.get("backend_type")).isEqualTo("ols");
        assertThat(secondPlant.get("short_form")).isEqualTo("NCIT_C14258");
        assertThat(secondPlant.get("descriptions"))
                .isEqualTo(new ArrayList<String>(List.of(new String[]{"Any living organism that typically synthesizes its food from inorganic substances, possesses cellulose cell walls, responds slowly and often permanently to a stimulus, lacks specialized sense organs and nervous system, and has no powers of locomotion. (EPA Terminology Reference System)"})));
        assertThat(secondPlant.get("label")).isEqualTo("Plant");
        assertThat(secondPlant.get("source")).isEqualTo("https://service.tib.eu/ts4tib/api");
        assertThat(secondPlant.get("type")).isEqualTo("class");
        assertThat(secondPlant.get("ontology")).isEqualTo("ncit");

        secondPlant = findByIriAndBackendType(responseList, "https://w3id.org/biolink/vocab/Plant", "ols2");
        assertThat(secondPlant.get("iri")).isEqualTo("https://w3id.org/biolink/vocab/Plant");
        assertThat(secondPlant.get("backend_type")).isEqualTo("ols2");
        assertThat(secondPlant.get("short_form")).isEqualTo("Plant");
//        assertThat(secondPlant.get("descriptions"))
//                .isEqualTo(new ArrayList<String>(List.of(new String[]{"Any living organism that typically synthesizes its food from inorganic substances, possesses cellulose cell walls, responds slowly and often permanently to a stimulus, lacks specialized sense organs and nervous system, and has no powers of locomotion. (EPA Terminology Reference System)"})));
        assertThat(secondPlant.get("label")).isEqualTo("plant");
        assertThat(secondPlant.get("source")).isEqualTo("https://www.ebi.ac.uk/ols4/api/v2");
        assertThat(secondPlant.get("type")).isEqualTo("class");
        assertThat(secondPlant.get("ontology")).isEqualTo("biolink");

        assertThat(responseList.stream().map(x -> x.get("backend_type")).distinct().sorted().collect(Collectors.toList()))
                .isEqualTo(configurationLoader.getDatabaseConfigs().stream().map(DatabaseConfig::getDatabase).sorted().distinct().collect(Collectors.toList()));
        assertThat(responseList.stream().map(x -> x.get("source_name")).distinct().sorted().toArray())
                .isEqualTo(new String[]{"agroportal", "agrovoc", "biodivportal", "ebi", "tib"});
    }

    @Test
    public void testSearchOlsSchema() {
        CommonRequestParams commonRequestParams = new CommonRequestParams();
        commonRequestParams.setTargetDbSchema(TargetDbSchema.ols);
        CompletableFuture<Object> r = searchService.performSearch("plant", commonRequestParams, null, null, null, apiAccessor);

        Map<String, Object> response = (Map<String, Object>) r.join();

        assertThat(response.containsKey("response")).isTrue();
        assertThat(response.containsKey("responseHeader")).isTrue();


        List<Map<String, Object>> responseList = (List<Map<String, Object>>) ((Map<String, Object>) response.get("response")).get("docs");

        assertThat(responseList).hasSize(100);

    }

    @Test
    public void testSearchJsonLdFormat() throws ExecutionException, InterruptedException {
        CommonRequestParams commonRequestParams = new CommonRequestParams();
        commonRequestParams.setFormat(ResponseFormat.jsonld);
        CompletableFuture<Object> r = searchService.performSearch("plant", commonRequestParams, null, null, null, apiAccessor);

        AggregatedApiResponse response = (AggregatedApiResponse) r.get();

        Map<String, Object> firstPlant = response.getCollection().get(0);
        assertThat(firstPlant.containsKey("@type")).isTrue();
        assertThat(firstPlant.containsKey("@context")).isTrue();
        //TODO add more assertions to check to @context content
    }


}
