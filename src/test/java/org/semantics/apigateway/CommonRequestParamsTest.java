package org.semantics.apigateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.semantics.apigateway.model.CommonRequestParams;
import org.semantics.apigateway.model.SemanticArtefact;
import org.semantics.apigateway.model.responses.AggregatedApiResponse;
import org.semantics.apigateway.model.responses.AggregatedResourceBody;
import org.semantics.apigateway.service.artefacts.ArtefactsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CommonRequestParamsTest extends ApplicationTestAbstract {

    @Autowired
    private ArtefactsService artefactsService;


    @BeforeEach
    public void setup() {
        mockApiAccessor("artefacts", artefactsService.getAccessor());
        this.responseClass = SemanticArtefact.class;
    }

    @Test
    public void testGetAllArtefactsWithDisplay() {
        CommonRequestParams params = new CommonRequestParams();
        params.setDisplay("short_form,backend_type,descriptions,synonyms,label");
        AggregatedApiResponse response = (AggregatedApiResponse) artefactsService.getArtefacts(params, null, null, apiAccessor);
        int index;
        List<Map<String, Object>> responseList = response.getCollection();

        index = indexOfShortFormAndBackendType(responseList, "AGROVOC", "ontoportal");
        Map<String, Object> object = responseList.get(index);
        Map<String, Object> expected = createOntoportalAgrovocFixture();
        assertThat(object.get("synonyms")).isEqualTo(expected.get("synonyms"));
        assertThat(object.get("label")).isEqualTo(expected.get("label"));
        assertThat(object.get("descriptions")).isEqualTo(expected.get("descriptions"));
        assertThat(object.keySet().stream().sorted().toList())
                .isEqualTo(Stream.of("descriptions", "synonyms", "label", "@type", "@context", "short_form", "backend_type").sorted().toList());
    }


    @Test
    public void testGetAllArtefactsWithNotDisplayEmptyValues() {
        CommonRequestParams params = new CommonRequestParams();
        params.setDisplayEmptyValues(false);
        AggregatedApiResponse response = (AggregatedApiResponse) artefactsService.getArtefacts(params, null, null, apiAccessor);
        int index;
        List<Map<String, Object>> responseList = response.getCollection();

        index = indexOfShortFormAndBackendType(responseList, "AGROVOC", "ontoportal");
        Map<String, Object> object = responseList.get(index);
        object.forEach((key, value) -> assertThat(AggregatedResourceBody.isEmpty(value)).isFalse());
    }
}
