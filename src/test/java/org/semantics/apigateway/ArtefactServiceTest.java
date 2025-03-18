package org.semantics.apigateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.semantics.apigateway.model.CommonRequestParams;
import org.semantics.apigateway.model.responses.AggregatedApiResponse;
import org.semantics.apigateway.service.artefacts.ArtefactsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

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
    public void testGetArtefact() {
        // Test ontoportal backend
        CommonRequestParams commonRequestParams = new CommonRequestParams();
        AggregatedApiResponse response = (AggregatedApiResponse) artefactsService.getArtefact("AGROVOC", commonRequestParams, apiAccessor);
        List<Map<String, Object>> responseList = response.getCollection();
        assertThat(responseList).hasSize(1);
        Map<String, Object> ontoportalItem = responseList.get(0);
        assertThat(ontoportalItem).containsAllEntriesOf(createOntoportalAgrovocFixture());

        // Test skosmos backend
        commonRequestParams.setDatabase("skosmos");
        response = (AggregatedApiResponse) artefactsService.getArtefact("AGROVOC", commonRequestParams, apiAccessor);
        responseList = response.getCollection();

        assertThat(responseList).hasSize(1);
        Map<String, Object> skosmosItem = responseList.get(0);
        assertThat(skosmosItem).containsAllEntriesOf(createSkosmosAgrovocFixture());

        // Test ols backend
        commonRequestParams.setDatabase("ols");
        response = (AggregatedApiResponse) artefactsService.getArtefact("AGROVOC", commonRequestParams, apiAccessor);
        responseList = response.getCollection();
        assertThat(responseList).hasSize(1);
        Map<String, Object> olsItem = responseList.get(0);
        assertThat(olsItem).containsAllEntriesOf(createOlsAgrovocFixture());
    }


}
