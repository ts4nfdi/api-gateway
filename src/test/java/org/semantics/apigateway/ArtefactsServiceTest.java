package org.semantics.apigateway;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.semantics.apigateway.config.DatabaseConfig;
import org.semantics.apigateway.model.responses.AggregatedApiResponse;
import org.semantics.apigateway.service.ArtefactsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ArtefactsServiceTest extends ApplicationTestAbstract {

    @Autowired
    private ArtefactsService artefactsService;


    @BeforeEach
    public void setup() {
        mockApiAccessor("artefacts", artefactsService.getAccessor());

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

    @Test
    public void testGetAllArtefacts() {
        CompletableFuture<Object> r = artefactsService.getArtefacts("", null, null, false, null, null, apiAccessor);

        AggregatedApiResponse response = (AggregatedApiResponse) r.join();

        List<Map<String, Object>> responseList = response.getCollection();

        assertThat(responseList.size()).isEqualTo(722);

        Map<String, Object> ontoportalItem = findByShortFormAndBackendType(responseList, "AAO", "ontoportal");
        assertThat(ontoportalItem.get("iri")).isEqualTo("http://cavoc.org/aao/");
        assertThat(ontoportalItem.get("backend_type")).isEqualTo("ontoportal");
        assertThat(ontoportalItem.get("short_form")).isEqualTo("AAO");
        assertThat(ontoportalItem.get("label")).isEqualTo("Agriculture Activity Ontology");
        assertThat(ontoportalItem.get("source")).isEqualTo("https://data.agroportal.lirmm.fr");
        assertThat(ontoportalItem.get("type")).isEqualTo("http://data.bioontology.org/metadata/OntologySubmission");
        assertThat(ontoportalItem.get("source_name")).isEqualTo("agroportal");
        assertThat(ontoportalItem.get("ontology")).isEqualTo("AAO");
        assertThat(ontoportalItem.get("synonyms")).isEqualTo(Collections.emptyList());
        assertThat(ontoportalItem.get("created")).isEqualTo("2015-05-07T00:00:00.000+00:00");
        assertThat(ontoportalItem.get("obsolete")).isEqualTo(false);
        assertThat(ontoportalItem.get("source_url")).isEqualTo("http://agroportal.lirmm.fr/ontologies/AAO");
        assertThat(ontoportalItem.get("modified")).isEqualTo("2024-05-08T00:00:00.000+00:00");
        assertThat(ontoportalItem.get("ontology_iri")).isEqualTo("http://cavoc.org/aao/");
        assertThat(ontoportalItem.get("version")).isNull();
        assertThat(((List<String>) ontoportalItem.get("descriptions")).get(0))
                .isEqualTo("Agriculture Activity ontology (AAO) is an ontology that describes agricultural work using attributes such as purpose, behavior, objective, and its attribute values.");


        Map<String, Object> skosmosItem = findByShortFormAndBackendType(responseList, "agrovoc", "skosmos");

        assertThat(skosmosItem.get("backend_type")).
                isEqualTo("skosmos");

        assertThat(skosmosItem.get("label")).

                isEqualTo("agrovoc");

        assertThat(skosmosItem.get("source")).

                isEqualTo("https://agrovoc.fao.org/browse/rest/v1");

        assertThat(skosmosItem.get("source_name")).

                isEqualTo("agrovoc");


        List<String> descriptionList2 = (List<String>) skosmosItem.get("descriptions");

        assertThat(descriptionList2).hasSize(1);
        assertThat(descriptionList2.get(0)).isEqualTo("AGROVOC Multilingual Thesaurus");


        ontoportalItem = findByShortFormAndBackendType(responseList, "bto", "ols2");
        assertThat(ontoportalItem.get("iri")).isEqualTo("http://purl.obolibrary.org/obo/bto.owl");
        assertThat(ontoportalItem.get("backend_type")).isEqualTo("ols2");
        assertThat(ontoportalItem.get("short_form")).isEqualTo("bto");
        assertThat(ontoportalItem.get("label")).isEqualTo("BRENDA tissue / enzyme source");
        assertThat(ontoportalItem.get("source")).isEqualTo("https://www.ebi.ac.uk/ols4/api/v2");
        assertThat(ontoportalItem.get("type")).isEqualTo("ontology");
        assertThat(ontoportalItem.get("source_name")).isEqualTo("ebi");
        assertThat(ontoportalItem.get("ontology")).isEqualTo("bto");
        assertThat(ontoportalItem.get("synonyms")).isEqualTo(Collections.emptyList());
        assertThat(ontoportalItem.get("created")).isNull();
        assertThat(ontoportalItem.get("obsolete")).isEqualTo(false);
        assertThat(ontoportalItem.get("source_url")).isNull();
        assertThat(ontoportalItem.get("modified")).isEqualTo("2025-02-28T14:59:32.399774659");
        assertThat(ontoportalItem.get("ontology_iri")).isEqualTo("http://purl.obolibrary.org/obo/bto.owl");
        assertThat(ontoportalItem.get("version")).isNull();
        assertThat(((List<String>) ontoportalItem.get("descriptions")).get(0))
                .isEqualTo("A structured controlled vocabulary for the source of an enzyme comprising tissues, cell lines, cell types and cell cultures.");


        assertThat(responseList.stream().map(x -> x.get("source_name")).distinct().sorted().toArray())
                .isEqualTo(new String[]{"agroportal", "agrovoc", "ebi", "tib"});
    }

}
