package org.semantics.apigateway;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.semantics.apigateway.config.DatabaseConfig;
import org.semantics.apigateway.model.CommonRequestParams;
import org.semantics.apigateway.model.responses.AggregatedApiResponse;
import org.semantics.apigateway.service.artefacts.ArtefactsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ArtefactServiceTest extends ApplicationTestAbstract {

    @Autowired
    private ArtefactsService artefactsService;


    @BeforeEach
    public void setup() {
        mockApiAccessor("artefact", artefactsService.getAccessor());
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
    public void testGetArtefact() {
        CommonRequestParams commonRequestParams = new CommonRequestParams();
        Object r = artefactsService.getArtefact("AGROVOC", commonRequestParams, apiAccessor);
        AggregatedApiResponse response = (AggregatedApiResponse) r;
        List<Map<String, Object>> responseList = response.getCollection();

        assertThat(responseList).hasSize(1);
        Map<String, Object> ontoportalItem = responseList.get(0);
        assertThat(ontoportalItem.get("iri")).isEqualTo("http://aims.fao.org/aos/agrovoc/");
        assertThat(ontoportalItem.get("backend_type")).isEqualTo("ontoportal");
        assertThat(ontoportalItem.get("short_form")).isEqualTo("AGROVOC");
        assertThat(ontoportalItem.get("label")).isEqualTo("AGROVOC");
        assertThat(ontoportalItem.get("source")).isEqualTo("https://data.agroportal.lirmm.fr");
        //TODO: fix the type to show SemanticArtefact
//        assertThat(ontoportalItem.get("type")).isEqualTo("class");
        assertThat(ontoportalItem.get("source_name")).isEqualTo("agroportal");
        assertThat(ontoportalItem.get("ontology")).isEqualTo("AGROVOC");

        List<String> descriptionList1 = (List<String>) ontoportalItem.get("descriptions");
        assertThat(descriptionList1).hasSize(1);
        assertThat(descriptionList1.get(0)).startsWith("AGROVOC is a multilingual and controlled vocabulary designed to cover concepts and terminology under FAO's areas of interest. ");


        commonRequestParams.setDatabase("skosmos");
        r = artefactsService.getArtefact("AGROVOC", commonRequestParams, apiAccessor);
        response = (AggregatedApiResponse) r;
        responseList = response.getCollection();

        assertThat(responseList).hasSize(1);
        Map<String, Object> skosmosItem = responseList.get(0);
        assertThat(skosmosItem.get("backend_type")).isEqualTo("skosmos");
        assertThat(skosmosItem.get("label")).isEqualTo("agrovoc");
        assertThat(skosmosItem.get("short_form")).isEqualTo("agrovoc");
        assertThat(skosmosItem.get("source")).isEqualTo("https://agrovoc.fao.org/browse/rest/v1");
        assertThat(skosmosItem.get("source_name")).isEqualTo("agrovoc");
        assertThat(skosmosItem.get("ontology")).isEqualTo("agrovoc");

        List<String> descriptionList2 = (List<String>) skosmosItem.get("descriptions");
        assertThat(descriptionList2).hasSize(1);
        assertThat(descriptionList2.get(0)).isEqualTo("AGROVOC Multilingual Thesaurus");


        commonRequestParams.setDatabase("ols");
        r = artefactsService.getArtefact("AGROVOC", commonRequestParams, apiAccessor);
        response = (AggregatedApiResponse) r;
        responseList = response.getCollection();
        assertThat(responseList).hasSize(1);
        Map<String, Object> olsItem = responseList.get(0);
        assertThat(olsItem.get("backend_type")).isEqualTo("ols");
        assertThat(olsItem.get("label")).isEqualTo("AGROVOC Multilingual Thesaurus");
        assertThat(olsItem.get("short_form")).isEqualTo("agrovoc");
        assertThat(olsItem.get("source")).isEqualTo("https://semanticlookup.zbmed.de/ols/api");
        assertThat(olsItem.get("source_name")).isEqualTo("zbmed");
        assertThat(olsItem.get("ontology")).isEqualTo("agrovoc");
    }

}
