package org.semantics.apigateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.semantics.apigateway.model.CommonRequestParams;
import org.semantics.apigateway.model.responses.AggregatedApiResponse;
import org.semantics.apigateway.service.artefacts.ArtefactsService;
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
    }


    @Test
    public void testGetArtefacts(){
        CommonRequestParams commonRequestParams = new CommonRequestParams();
        commonRequestParams.setDatabase("ontoportal");
        AggregatedApiResponse response = (AggregatedApiResponse) artefactsService.getArtefact("AGROVOC", commonRequestParams, apiAccessor);
        assertMapEquality(response, createOntoportalAgrovocFixture());
    }


    @Test
    public void testGetAretefactsSkosmos(){
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
}
