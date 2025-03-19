package org.semantics.apigateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.semantics.apigateway.model.CommonRequestParams;
import org.semantics.apigateway.model.responses.AggregatedApiResponse;
import org.semantics.apigateway.service.artefacts.ArtefactsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

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
        AggregatedApiResponse response = (AggregatedApiResponse) artefactsService.getArtefact("AGROVOC", commonRequestParams, apiAccessor);
        assertThat(response.getCollection()).hasSize(1);
        assertThat(response.getCollection().get(0)).containsAllEntriesOf(createOntoportalAgrovocFixture());
    }


    @Test
    public void testGetAretefactsSkosmos(){
        CommonRequestParams commonRequestParams = new CommonRequestParams();
        commonRequestParams.setDatabase("skosmos");
        AggregatedApiResponse response = (AggregatedApiResponse) artefactsService.getArtefact("AGROVOC", commonRequestParams, apiAccessor);
        assertThat(response.getCollection()).hasSize(1);
        assertThat(response.getCollection().get(0)).containsAllEntriesOf(createSkosmosAgrovocFixture());
    }

    @Test
    public void testGetArtefactsOls() {
        CommonRequestParams commonRequestParams = new CommonRequestParams();
        commonRequestParams.setDatabase("ols");
        AggregatedApiResponse response = (AggregatedApiResponse) artefactsService.getArtefact("AGROVOC", commonRequestParams, apiAccessor);
        assertThat(response.getCollection()).hasSize(1);
        assertThat(response.getCollection().get(0)).containsAllEntriesOf(createOlsAgrovocFixture());
    }
}
