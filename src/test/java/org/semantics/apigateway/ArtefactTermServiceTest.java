package org.semantics.apigateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.semantics.apigateway.model.CommonRequestParams;
import org.semantics.apigateway.model.responses.AggregatedApiResponse;
import org.semantics.apigateway.service.artefacts.ArtefactsDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ArtefactTermServiceTest extends ApplicationTestAbstract {

    @Autowired
    private ArtefactsDataService artefactsService;


    @BeforeEach
    public void setup() {
        mockApiAccessor("artefact_term", artefactsService.getAccessor());
    }

    @Test
    public void testGetArtefactTerm() {
        AggregatedApiResponse response = (AggregatedApiResponse) artefactsService.getArtefactTerm("AGROVOC", "http://aims.fao.org/aos/agrovoc/c_330834", new CommonRequestParams(), apiAccessor);
        List<Map<String, Object>> responseList = response.getCollection();
        assertThat(responseList).hasSize(1);

        Map<String, Object> skosmosItem = responseList.get(0);
        assertThat(skosmosItem).containsAllEntriesOf(createSkosmosAgrovocTerm());


        CommonRequestParams params = new CommonRequestParams();
        params.setDatabase("ontoportal");
        response = (AggregatedApiResponse) artefactsService.getArtefactTerm("AGROVOC", "http://aims.fao.org/aos/agrovoc/c_330834", params, apiAccessor);
        responseList = response.getCollection();
        assertThat(responseList).hasSize(1);
        assertThat(responseList.get(0)).containsAllEntriesOf(createOntoPortalAgrovocTermFixture());
    }

    private Map<String, Object> createSkosmosAgrovocTerm() {
        Map<String, Object> fixture = new HashMap<>();
        fixture.put("iri", "http://aims.fao.org/aos/agrovoc/c_330834");
        fixture.put("backend_type", "skosmos");
        fixture.put("label", "activities");
        fixture.put("source", "https://agrovoc.fao.org/browse/rest/v1");
        fixture.put("source_name", "agrovoc");
//        fixture.put("ontology", "agrovoc");
//        fixture.put("descriptions", List.of("AGROVOC Multilingual Thesaurus"));
        fixture.put("synonyms", Collections.emptyList());
        return fixture;
    }

    private Map<String,Object> createOntoPortalAgrovocTermFixture() {
        Map<String, Object> fixture = new HashMap<>();
        fixture.put("iri", "http://aims.fao.org/aos/agrovoc/c_330834");
        fixture.put("backend_type", "ontoportal");
        fixture.put("short_form", "c_330834");
        fixture.put("label", "activities");
        fixture.put("source", "https://data.agroportal.lirmm.fr");
        fixture.put("source_name", "agroportal");
//        fixture.put("ontology", "agrovoc");
        fixture.put("descriptions", List.of(
                "http://aims.fao.org/aos/agrovoc/xDef_d8a81e42",
                "http://aims.fao.org/aos/agrovoc/xDef_47a14ae7"
        ));
        fixture.put("synonyms", Collections.emptyList());

//        assertThat(ontoportalItem.get("type")).isEqualTo("class"); // TODO implement default value logic
        return fixture;
    }

}
