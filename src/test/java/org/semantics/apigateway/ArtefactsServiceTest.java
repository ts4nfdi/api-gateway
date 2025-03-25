package org.semantics.apigateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.semantics.apigateway.model.CommonRequestParams;
import org.semantics.apigateway.model.SemanticArtefact;
import org.semantics.apigateway.model.responses.AggregatedApiResponse;
import org.semantics.apigateway.service.artefacts.ArtefactsService;
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
public class ArtefactsServiceTest extends ApplicationTestAbstract {

    @Autowired
    private ArtefactsService artefactsService;


    @BeforeEach
    public void setup() {
        mockApiAccessor("artefacts", artefactsService.getAccessor());
        this.responseClass = SemanticArtefact.class;
    }

    @Test
    public void testGetAllArtefacts() {
        AggregatedApiResponse response = (AggregatedApiResponse) artefactsService.getArtefacts(new CommonRequestParams(), null, null, apiAccessor);
        int index;
        List<Map<String, Object>> responseList = response.getCollection();


        index = indexOfShortFormAndBackendType(responseList, "AGROVOC", "ontoportal");
        assertMapEquality(response, createOntoportalAgrovocFixture(), 724, index);

        index = indexOfShortFormAndBackendType(responseList, "agrovoc", "skosmos");
        Map<String, Object> skosmosExpected = createSkosmosAgrovocFixture();
        skosmosExpected.put("iri", "agrovoc");
        assertMapEquality(response, skosmosExpected, 724, index);

        index = indexOfShortFormAndBackendType(responseList, "bto", "ols");
        assertMapEquality(response, createOlsFixture(), 724, index);

        index = indexOfShortFormAndBackendType(responseList, "bto", "ols2");
        assertMapEquality(response, createOls2Fixture(), 724, index);

        index = indexOfShortFormAndBackendType(responseList, "GND", "gnd");
        assertMapEquality(response, createGndFixture(), 724, index);

        assertThat(responseList.stream().map(x -> x.get("source_name")).distinct().sorted().toArray())
                .isEqualTo(new String[]{"agroportal", "agrovoc", "ebi", "gnd", "tib"});
    }


    private Map<String,Object> createOlsFixture(){
        Map<String, Object> fixture = new HashMap<>();
        fixture.put("iri", "http://purl.obolibrary.org/obo/bto.owl");
        fixture.put("source", "https://service.tib.eu/ts4tib/api");
        fixture.put("backend_type", "ols");
        fixture.put("short_form", "bto");
        fixture.put("label", "The BRENDA Tissue Ontology (BTO)");
        fixture.put("source_name", "tib");
        fixture.put("synonyms", Collections.emptyList());
        fixture.put("created", null);
        fixture.put("obsolete", false);
        fixture.put("source_url", "https://service.tib.eu:443/ts4tib/api/ontologies/bto");
        fixture.put("modified", null);
        fixture.put("version", "2021-10-26");
        fixture.put("descriptions", List.of(
                "A structured controlled vocabulary for the source of an enzyme comprising tissues, cell lines, cell types and cell cultures."
        ));
        fixture.put("type", null); //TODO: check if this is correct
        return fixture;
    }
    private Map<String, Object> createOls2Fixture() {
        Map<String, Object> fixture = new HashMap<>();
        fixture.put("iri", "http://purl.obolibrary.org/obo/bto.owl");
        fixture.put("backend_type", "ols2");
        fixture.put("short_form", "bto");
        fixture.put("label", "BRENDA tissue / enzyme source");
        fixture.put("source", "https://www.ebi.ac.uk/ols4/api/v2");
        fixture.put("type", "ontology");
        fixture.put("source_name", "ebi");
        fixture.put("synonyms", Collections.emptyList());
        fixture.put("created", null);
        fixture.put("obsolete", false);
        fixture.put("source_url", null);
        fixture.put("modified", "2025-03-16T21:34:29.847078927");
        fixture.put("version", null);
        fixture.put("descriptions", List.of(
                "A structured controlled vocabulary for the source of an enzyme comprising tissues, cell lines, cell types and cell cultures."
        ));
        return fixture;
    }

}
