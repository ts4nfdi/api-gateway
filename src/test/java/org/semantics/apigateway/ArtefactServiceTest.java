package org.semantics.apigateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.semantics.apigateway.artefacts.metadata.ArtefactsService;
import org.semantics.apigateway.model.CommonRequestParams;
import org.semantics.apigateway.model.SemanticArtefact;
import org.semantics.apigateway.model.responses.AggregatedApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ArtefactServiceTest extends ApplicationTestAbstract {

    @Autowired
    private ArtefactsService artefactsService;


    @BeforeEach
    public void setup() {
        mockApiAccessor("artefact", artefactsService.getAccessor());
        this.responseClass = SemanticArtefact.class;
    }


    @Test
    public void testGetArtefacts(){
        CommonRequestParams commonRequestParams = new CommonRequestParams();
        commonRequestParams.setDatabase("ontoportal");
        AggregatedApiResponse response = (AggregatedApiResponse) artefactsService.getArtefact("AGROVOC", commonRequestParams, apiAccessor);
        assertMapEquality(response, createOntoportalAgrovocFixture());
    }


    @Test
    public void testGetArtefactsSkosmos(){
        CommonRequestParams commonRequestParams = new CommonRequestParams();
        commonRequestParams.setDatabase("skosmos");
        AggregatedApiResponse response = (AggregatedApiResponse) artefactsService.getArtefact("AGROVOC", commonRequestParams, apiAccessor);
        assertMapEquality(response, createSkosmosAgrovocFixture());
    }

    @Test
    public void testGetArtefactsOls() {
        CommonRequestParams commonRequestParams = new CommonRequestParams();
        commonRequestParams.setDatabase("ols");
        AggregatedApiResponse response = (AggregatedApiResponse) artefactsService.getArtefact("AGROVOC", commonRequestParams, apiAccessor);
        assertMapEquality(response, createOlsAgrovocFixture());
    }

    @Test
    public void testGetArtefactGND() {
        CommonRequestParams commonRequestParams = new CommonRequestParams();
        commonRequestParams.setDatabase("gnd");
        AggregatedApiResponse response = (AggregatedApiResponse) artefactsService.getArtefact("gnd", commonRequestParams, apiAccessor);
        assertMapEquality(response, createGndFixture());
    }


    @Test
    public void testGetArtefactJSkos() {
        CommonRequestParams commonRequestParams = new CommonRequestParams();
        commonRequestParams.setDatabase("jskos");
        AggregatedApiResponse response = (AggregatedApiResponse) artefactsService.getArtefact("gender", commonRequestParams, apiAccessor);
        assertMapEquality(response, createDanteFixture());
    }


    @Test
    public void testGetArtefactJSkos2() {
        CommonRequestParams commonRequestParams = new CommonRequestParams();
        commonRequestParams.setDatabase("jskos2");
        AggregatedApiResponse response = (AggregatedApiResponse) artefactsService.getArtefact("EuroVoc", commonRequestParams, apiAccessor);
        assertMapEquality(response, createColiConc());
    }
}
