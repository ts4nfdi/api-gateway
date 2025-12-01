package org.semantics.apigateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.semantics.apigateway.artefacts.search.SearchService;
import org.semantics.apigateway.model.CommonRequestParams;
import org.semantics.apigateway.model.RDFResource;
import org.semantics.apigateway.model.TargetDbSchema;
import org.semantics.apigateway.model.responses.AggregatedApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        this.responseClass = RDFResource.class;
    }

    @Test
    public void testSearchAllDatabases() {
        CommonRequestParams commonRequestParams = new CommonRequestParams();
        AggregatedApiResponse response = (AggregatedApiResponse) searchService.performSearch("plant", commonRequestParams, null, null, apiAccessor);
        int index;
        List<Map<String, Object>> responseList = response.getCollection();

        //TODO need a better testing of the ranking
        assertThat(responseList.stream()
                .allMatch(x -> x.containsKey("label") && x.get("label").toString().toLowerCase().startsWith("plant")))
                .isTrue();

        index = indexOfIriAndBackendType(responseList, "http://sweetontology.net/matrPlant/Plant", "ontoportal");
        assertMapEquality(response, createOntoPortalPlantFixture(), 100, index);

        index = indexOfIriAndBackendType(responseList, "http://purl.obolibrary.org/obo/NCIT_C14258", "ols");
        assertMapEquality(response, createOlsPlantFixture(), 100, index);

        index = indexOfIriAndBackendType(responseList, "https://w3id.org/biolink/vocab/Plant", "ols2");
        assertMapEquality(response, createOls2Fixture(), 100, index);

        assertThat(responseList.stream().map(x -> x.get("backend_type")).distinct().sorted().toArray())
                .isEqualTo(new String[]{"ols", "ols2", "ontoportal", "skosmos"});
        assertThat(responseList.stream().map(x -> x.get("source_name")).distinct().sorted().toArray())
                .isEqualTo(new String[]{"agroportal", "agrovoc", "biodivportal", "ebi", "tib"});
    }

    @Test
    public void testSearchOlsSchema() {
        CommonRequestParams commonRequestParams = new CommonRequestParams();
        commonRequestParams.setTargetDbSchema(TargetDbSchema.ols);

        Map<String, Object> response = ((AggregatedApiResponse) searchService.performSearch("plant", commonRequestParams, null, null, apiAccessor)).getCollection().get(0);

        assertThat(response.containsKey("response")).isTrue();
        assertThat(response.containsKey("responseHeader")).isTrue();
        List<Map<String, Object>> responseList = (List<Map<String, Object>>) ((Map<String, Object>) response.get("response")).get("docs");

        assertThat(responseList).hasSize(100);
    }

    @Test
    public void testSearchGnd() {
        CommonRequestParams commonRequestParams = new CommonRequestParams();
        commonRequestParams.setDatabase("gnd");
        AggregatedApiResponse response = (AggregatedApiResponse) searchService.performSearch("London", commonRequestParams, null, null, apiAccessor);
        assertMapEquality(response, createGndLondonFixture(), 10);
    }

    private Map<String, Object> createOntoPortalPlantFixture() {
        Map<String, Object> firstPlant = new HashMap<>();
        firstPlant.put("iri", "http://sweetontology.net/matrPlant/Plant");
        firstPlant.put("backend_type", "ontoportal");
        firstPlant.put("short_form", "Plant");
        firstPlant.put("label", "plant");
        firstPlant.put("source", "https://data.biodivportal.gfbio.org");
        firstPlant.put("type", "http://www.w3.org/2002/07/owl#Class");
        firstPlant.put("ontology", "SWEET");
        firstPlant.put("ontology_iri", "https://data.biodivportal.gfbio.dev/ontologies/SWEET");
        return firstPlant;
    }

    private Map<String, Object> createOlsPlantFixture() {
        Map<String, Object> secondPlant = new HashMap<>();
        secondPlant.put("iri", "http://purl.obolibrary.org/obo/NCIT_C14258");
        secondPlant.put("backend_type", "ols");
        secondPlant.put("short_form", "NCIT_C14258");
        secondPlant.put("label", "Plant");
        secondPlant.put("source", "https://api.terminology.tib.eu/api");
        secondPlant.put("@type", new RDFResource().getTypeURI()); //TODO: should be owl:Class in future
        secondPlant.put("@id", secondPlant.get("iri"));
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
        thirdPlant.put("@type", new RDFResource().getTypeURI()); //TODO: should be owl:Class in future
        thirdPlant.put("ontology", "biolink");
        return thirdPlant;
    }

    private Map<String, Object> createGndLondonFixture() {
        Map<String, Object> gndPlant = new HashMap<>();
        gndPlant.put("iri", "https://d-nb.info/gnd/4074335-4");
        gndPlant.put("backend_type", "gnd");
        gndPlant.put("synonyms", List.of("Londen", "Corporation of London", "Augusta Trinobantum", "Landan", "Londres", "Londinum", "County of London", "Lundonia", "Londra", "Londyn", "Greater London", "London (Great Britain)", "Londinium", "Westminster", "Lundun"));
        gndPlant.put("source", "https://lobid.org");
        gndPlant.put("label", "London");
        gndPlant.put("type", "AuthorityResource");
        gndPlant.put("descriptions", List.of("Hauptstadt des Vereinigten Königreichs von Großbritannien und Nordirland, in Mittelsteinzeit besiedelt, 43 n. Chr. von Römern gegründet; das County of London war 1889-1965 Verwaltungsgrafschaft u. zeremonielle Grafschaft"));
        gndPlant.put("source_url", "https://d-nb.info/gnd/4074335-4");
        gndPlant.put("short_form", "4074335-4");
        gndPlant.put("source_name", "gnd");
        return gndPlant;
    }

}
