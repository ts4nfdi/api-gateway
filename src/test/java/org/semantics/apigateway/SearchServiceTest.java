package org.semantics.apigateway;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SearchServiceTest extends ApplicationTestAbstract {

    @Autowired
    private SearchService searchService;


    @BeforeEach
    public void setup() {
        mockApiAccessor("search", searchService.getAccessor());
    }

    @Test
    public void testSearchAllDatabases() {
        CommonRequestParams commonRequestParams = new CommonRequestParams();
        AggregatedApiResponse response = (AggregatedApiResponse) searchService.performSearch("plant", commonRequestParams, null, null, null, apiAccessor);

        List<Map<String, Object>> responseList = response.getCollection();

        assertThat(responseList).hasSize(100);

        assertThat(responseList.stream().allMatch(x -> x.containsKey("label") && x.get("label").toString().toLowerCase().startsWith("plant"))).isTrue();

        //TODO need a better testing of the ranking
        Map<String, Object> firstPlant = findByIriAndBackendType(responseList, "http://sweetontology.net/matrPlant/Plant", "ontoportal");
        assertThat(firstPlant).containsAllEntriesOf(createOntoPortalPlantFixture());

        Map<String, Object> secondPlant = findByIriAndBackendType(responseList, "http://purl.obolibrary.org/obo/NCIT_C14258", "ols");
        assertThat(secondPlant).containsAllEntriesOf(createOlsPlantFixture());

        secondPlant = findByIriAndBackendType(responseList, "https://w3id.org/biolink/vocab/Plant", "ols2");
        assertThat(secondPlant).containsAllEntriesOf(createOls2Fixture());

        assertThat(responseList.stream().map(x -> x.get("backend_type")).distinct().sorted().collect(Collectors.toList()))
                .isEqualTo(configurationLoader.getDatabaseConfigs().stream().map(DatabaseConfig::getDatabase).sorted().distinct().collect(Collectors.toList()));
        assertThat(responseList.stream().map(x -> x.get("source_name")).distinct().sorted().toArray())
                .isEqualTo(new String[]{"agroportal", "agrovoc", "biodivportal", "ebi", "tib"});
    }

    @Test
    public void testSearchOlsSchema() {
        CommonRequestParams commonRequestParams = new CommonRequestParams();
        commonRequestParams.setTargetDbSchema(TargetDbSchema.ols);

        Map<String, Object> response = (Map<String, Object>) searchService.performSearch("plant", commonRequestParams, null, null, null, apiAccessor);

        assertThat(response.containsKey("response")).isTrue();
        assertThat(response.containsKey("responseHeader")).isTrue();
        List<Map<String, Object>> responseList = (List<Map<String, Object>>) ((Map<String, Object>) response.get("response")).get("docs");

        assertThat(responseList).hasSize(100);

    }

    @Test
    public void testSearchJsonLdFormat() throws ExecutionException, InterruptedException {
        CommonRequestParams commonRequestParams = new CommonRequestParams();
        commonRequestParams.setFormat(ResponseFormat.jsonld);
        AggregatedApiResponse response = (AggregatedApiResponse) searchService.performSearch("plant", commonRequestParams, null, null, null, apiAccessor);

        Map<String, Object> firstPlant = response.getCollection().get(0);
        assertThat(firstPlant.containsKey("@type")).isTrue();
        assertThat(firstPlant.containsKey("@context")).isTrue();
        //TODO add more assertions to check to @context content
    }


    private Map<String, Object> createOntoPortalPlantFixture() {
        Map<String, Object> firstPlant = new HashMap<>();
        firstPlant.put("iri", "http://sweetontology.net/matrPlant/Plant");
        firstPlant.put("backend_type", "ontoportal");
//        firstPlant.put("short_form", null); // TODO implement default value logic
        firstPlant.put("label", "plant");
        firstPlant.put("source", "https://data.biodivportal.gfbio.org");
//        firstPlant.put("type", null); // TODO implement default value logic
//        firstPlant.put("ontology", null); // TODO implement default value logic
        return firstPlant;
    }

    private Map<String, Object> createOlsPlantFixture() {
        Map<String, Object> secondPlant = new HashMap<>();
        secondPlant.put("iri", "http://purl.obolibrary.org/obo/NCIT_C14258");
        secondPlant.put("backend_type", "ols");
        secondPlant.put("short_form", "NCIT_C14258");
        secondPlant.put("label", "Plant");
        secondPlant.put("source", "https://service.tib.eu/ts4tib/api");
        secondPlant.put("type", "class");
        secondPlant.put("ontology", "ncit");
        secondPlant.put("descriptions", List.of("Any living organism that typically synthesizes its food from inorganic substances, possesses cellulose cell walls, responds slowly and often permanently to a stimulus, lacks specialized sense organs and nervous system, and has no powers of locomotion. (EPA Terminology Reference System)"));
        return secondPlant;
    }

    private Map<String, Object> createOls2Fixture() {
        Map<String, Object> thirdPlant = new HashMap<>();
        thirdPlant.put("iri", "https://w3id.org/biolink/vocab/Plant");
        thirdPlant.put("backend_type", "ols2");
        thirdPlant.put("short_form", "Plant");
        thirdPlant.put("label", "plant");
        thirdPlant.put("source", "https://www.ebi.ac.uk/ols4/api/v2");
        thirdPlant.put("type", "class");
        thirdPlant.put("ontology", "biolink");
//        assertThat(secondPlant.get("descriptions"))
//                .isEqualTo(new ArrayList<String>(List.of(new String[]{"Any living organism that typically synthesizes its food from inorganic substances, possesses cellulose cell walls, responds slowly and often permanently to a stimulus, lacks specialized sense organs and nervous system, and has no powers of locomotion. (EPA Terminology Reference System)"})));
        return thirdPlant;
    }


}
