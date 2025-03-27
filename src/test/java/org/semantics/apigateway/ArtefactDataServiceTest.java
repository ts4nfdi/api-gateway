package org.semantics.apigateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.semantics.apigateway.model.CommonRequestParams;
import org.semantics.apigateway.model.RDFResource;
import org.semantics.apigateway.model.responses.AggregatedApiResponse;
import org.semantics.apigateway.service.artefacts.ArtefactsDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ArtefactDataServiceTest extends ApplicationTestAbstract {

    @Autowired
    private ArtefactsDataService artefactsService;

    public ArtefactDataServiceTest() {
        super();
        this.responseClass = RDFResource.class;
    }

    @BeforeEach
    public void setupClass() {
        this.responseClass = RDFResource.class;
    }

    @Test
    public void testGetTerm() {
        mockApiAccessor("artefact_term", artefactsService.getAccessor());
        CommonRequestParams params = new CommonRequestParams();
        params.setDatabase("skosmos");
        AggregatedApiResponse response = (AggregatedApiResponse) artefactsService.getArtefactTerm("AGROVOC", "http://aims.fao.org/aos/agrovoc/c_330834", params, apiAccessor);
        assertMapEquality(response, createSkosmosAgrovocTerm());

        params = new CommonRequestParams();
        params.setDatabase("ontoportal");
        response = (AggregatedApiResponse) artefactsService.getArtefactTerm("AGROVOC", "http://aims.fao.org/aos/agrovoc/c_330834", params, apiAccessor);
        assertMapEquality(response, createOntoPortalAgrovocTermFixture());

        params = new CommonRequestParams();
        params.setDatabase("ols2");
        response = (AggregatedApiResponse) artefactsService.getArtefactTerm("ncbitaxon", "http://purl.obolibrary.org/obo/NCBITaxon_2", params, apiAccessor);
        assertMapEquality(response, createOls2NCBITaxonFixture());

        params = new CommonRequestParams();
        params.setDatabase("ols");
        response = (AggregatedApiResponse) artefactsService.getArtefactTerm("ncbitaxon", "http://purl.obolibrary.org/obo/NCBITaxon_2", params, apiAccessor);
        assertMapEquality(response, createNCBITaxonFixture());

        params = new CommonRequestParams();
        params.setDatabase("gnd");
        response = (AggregatedApiResponse) artefactsService.getArtefactTerm("gnd", "4074335-4", params, apiAccessor);
        assertMapEquality(response, createGndTermFixture());
    }

    @Test
    public void testGetProperty() {
        mockApiAccessor("artefact_property", artefactsService.getAccessor());

        CommonRequestParams params = new CommonRequestParams();
        params.setDatabase("ontoportal");
        AggregatedApiResponse response = (AggregatedApiResponse) artefactsService.getArtefactProperty("AGROVOC", "http://purl.org/dc/terms/abstract", params, apiAccessor);
        assertMapEquality(response, createOntoPortalAgrovocPropertyFixture());


        params = new CommonRequestParams();
        params.setDatabase("ols");
        response = (AggregatedApiResponse) artefactsService.getArtefactProperty("NCBITAXON", "http://purl.obolibrary.org/obo/ncbitaxon#has_rank", params, apiAccessor);
        assertMapEquality(response, createOlsNCBITaxonPropertyFixture());

        params = new CommonRequestParams();
        params.setDatabase("ols2");
        response = (AggregatedApiResponse) artefactsService.getArtefactProperty("NCBITAXON", "http://purl.obolibrary.org/obo/ncbitaxon#has_rank", params, apiAccessor);
        assertMapEquality(response, createOls2NCBITaxonPropertyFixture());
    }

    @Test
    public void testGetCollection() {
        mockApiAccessor("artefact_collection", artefactsService.getAccessor());
        AggregatedApiResponse response = (AggregatedApiResponse) artefactsService.getArtefactCollection("INRAETHES", "http://opendata.inrae.fr/thesaurusINRAE/gr_6c79e7c5", new CommonRequestParams(), apiAccessor);
        assertMapEquality(response, createOntoPortalINRAEThesCollectionFixture());
    }

    @Test
    public void testGetIndividuals() {
        mockApiAccessor("artefact_individual", artefactsService.getAccessor());
        CommonRequestParams params = new CommonRequestParams();
        params.setDatabase("ols2");
        AggregatedApiResponse response = (AggregatedApiResponse) artefactsService.getArtefactIndividual("FOODON", "http://purl.obolibrary.org/obo/GAZ_00000464", new CommonRequestParams(), apiAccessor);
        assertMapEquality(response, createOls2FoodOnInstanceFixture());

        params = new CommonRequestParams();
        params.setDatabase("ontoportal");
        response = (AggregatedApiResponse) artefactsService.getArtefactIndividual("FOODON", "http://purl.obolibrary.org/obo/GAZ_00000464", params, apiAccessor);
        assertMapEquality(response, createOntoPortalFoodOnInstanceFixture());

        params = new CommonRequestParams();
        params.setDatabase("ols");
        response = (AggregatedApiResponse) artefactsService.getArtefactIndividual("FOODON", "http://purl.obolibrary.org/obo/GAZ_00000464", params, apiAccessor);
        assertMapEquality(response, createOlsFoodOnInstanceFixture());
    }

    @Test
    public void testGetScheme() {
        mockApiAccessor("artefact_scheme", artefactsService.getAccessor());
        AggregatedApiResponse response = (AggregatedApiResponse) artefactsService.getArtefactScheme("INRAETHES", "http://opendata.inrae.fr/thesaurusINRAE/mt_50", new CommonRequestParams(), apiAccessor);
        assertMapEquality(response, createOntoPortalInraeScheme());
    }

    private Map<String, Object> createOls2NCBITaxonPropertyFixture() {
        Map<String, Object> map = createOlsNCBITaxonPropertyFixture();
        map.put("backend_type", "ols2");
        map.put("source", "https://www.ebi.ac.uk/ols4/api/v2");
        map.put("source_name", "ebi");
        map.put("type", "property");
        map.put("source_url", null); //TODO: add source_url
        return map;
    }

    private Map<String, Object> createOlsNCBITaxonPropertyFixture() {
        Map<String, Object> map = new HashMap<>();
        map.put("iri", "http://purl.obolibrary.org/obo/ncbitaxon#has_rank");
        map.put("backend_type", "ols");
        map.put("synonyms", Collections.emptyList());
        map.put("created", null);
        map.put("obsolete", false);
        map.put("source", "https://semanticlookup.zbmed.de/ols/api");
        map.put("label", "has_rank");
        map.put("type", null); // TODO: fix this
        map.put("descriptions", List.of("A metadata relation between a class and its taxonomic rank (eg species, family)",
                "This is an abstract class for use with the NCBI taxonomy to name the depth of the node within the tree. The link between the node term and the rank is only visible if you are using an obo 1.3 aware browser/editor; otherwise this can be ignored"));
        map.put("version", null);
        map.put("source_url", "https://semanticlookup.zbmed.de/ols/api/ontologies/ncbitaxon/properties/http%253A%252F%252Fpurl.obolibrary.org%252Fobo%252Fncbitaxon%2523has_rank?lang=en");
        map.put("short_form", "has_rank");
        map.put("modified", null);
        map.put("ontology_iri", "http://purl.obolibrary.org/obo/ncbitaxon.owl");
        map.put("source_name", "zbmed");
        map.put("ontology", "ncbitaxon");
        return map;
    }

    private Map<String, Object> createOls2NCBITaxonFixture() {
        Map<String, Object> map = createNCBITaxonFixture();
        map.put("backend_type", "ols2");
        map.put("source", "https://www.ebi.ac.uk/ols4/api/v2");
        map.put("source_name", "ebi");
        map.put("type", "class");
        map.put("source_url", null); //TODO: add source_url
        return map;
    }

    private Map<String, Object> createNCBITaxonFixture() {
        Map<String, Object> result = new HashMap<>();
        result.put("iri", "http://purl.obolibrary.org/obo/NCBITaxon_2");
        result.put("backend_type", "ols");
        result.put("synonyms", List.of("eubacteria",
                "Bacteria (ex Cavalier-Smith 1987)",
                "Bacteria Woese et al. 2024",
                "Bacteriobiota",
                "Monera",
                "Procaryotae",
                "Prokaryota",
                "Prokaryotae",
                "bacteria",
                "prokaryote",
                "prokaryotes"));
        result.put("created", null);
        result.put("obsolete", false);
        result.put("source", "https://semanticlookup.zbmed.de/ols/api");
        result.put("label", "Bacteria");
        result.put("type", null); //TODO: fix this
        result.put("descriptions", Collections.emptyList());
        result.put("version", null);
        result.put("source_url", "https://semanticlookup.zbmed.de/ols/api/ontologies/ncbitaxon/terms/http%253A%252F%252Fpurl.obolibrary.org%252Fobo%252FNCBITaxon_2?lang=en");
        result.put("short_form", "NCBITaxon_2");
        result.put("modified", null);
        result.put("ontology_iri", "http://purl.obolibrary.org/obo/ncbitaxon.owl");
        result.put("source_name", "zbmed");
        result.put("ontology", "ncbitaxon");
        return result;
    }

    private Map<String, Object> createOntoPortalAgrovocPropertyFixture() {
        Map<String, Object> map = new HashMap<>();
        map.put("iri", "http://purl.org/dc/terms/abstract");
        map.put("label", "Abstract");
        map.put("backend_type", "ontoportal");
        map.put("synonyms", Collections.emptyList());
        map.put("descriptions", List.of("A summary of the resource."));
        map.put("obsolete", false);
        map.put("source", "https://data.agroportal.lirmm.fr");
        map.put("type", "http://www.w3.org/2002/07/owl#AnnotationProperty");
        map.put("version", null); //TODO: to remove maybe
        map.put("source_url", null); //TODO: add source_url
        map.put("short_form", "abstract");
        map.put("created", null);
        map.put("modified", null);
        map.put("ontology_iri", null); //TODO: add ontology_iri
        map.put("source_name", "agroportal");
        map.put("ontology", "https://data.agroportal.lirmm.fr/ontologies/AGROVOC");
        return map;
    }

    private Map<String, Object> createSkosmosAgrovocTerm() {
        Map<String, Object> fixture = new HashMap<>();
        fixture.put("iri", "http://aims.fao.org/aos/agrovoc/c_330834");
        fixture.put("backend_type", "skosmos");
        fixture.put("label", "activities");
        fixture.put("source", "https://agrovoc.fao.org/browse/rest/v1");
        fixture.put("source_name", "agrovoc");
        fixture.put("synonyms", Collections.emptyList());
        fixture.put("descriptions", null); //TODO: should be empty array or not empty
        fixture.put("created", null);
        fixture.put("modified", null);
        fixture.put("obsolete", false);
        fixture.put("source_url", null);
        fixture.put("version", null);
        fixture.put("ontology_iri", null); //TODO: fix this
        fixture.put("short_form", "c_330834");
        fixture.put("ontology", null);
        return fixture;
    }

    private Map<String, Object> createOntoPortalAgrovocTermFixture() {
        Map<String, Object> fixture = new HashMap<>();
        fixture.put("iri", "http://aims.fao.org/aos/agrovoc/c_330834");
        fixture.put("backend_type", "ontoportal");
        fixture.put("short_form", "c_330834");
        fixture.put("label", "activities");
        fixture.put("source", "https://data.agroportal.lirmm.fr");
        fixture.put("source_name", "agroportal");
        fixture.put("ontology", "https://data.agroportal.lirmm.fr/ontologies/AGROVOC");
        fixture.put("descriptions", List.of("http://aims.fao.org/aos/agrovoc/xDef_d8a81e42", "http://aims.fao.org/aos/agrovoc/xDef_47a14ae7"));
        fixture.put("synonyms", Collections.emptyList());
        fixture.put("created", "2008-09-25T00:00:00+00:00");
        fixture.put("modified", "2024-09-13T14:08:17+00:00");
        fixture.put("obsolete", false);
        fixture.put("source_url", "http://agroportal.lirmm.fr/ontologies/AGROVOC?p=classes&conceptid=http%3A%2F%2Faims.fao.org%2Faos%2Fagrovoc%2Fc_330834");
        fixture.put("version", null);
        fixture.put("ontology_iri", null); //TODO: fix this
        fixture.put("type", "http://www.w3.org/2004/02/skos/core#Concept"); // TODO implement default value logic
        return fixture;
    }

    private Map<String, Object> createOntoPortalINRAEThesCollectionFixture() {
        Map<String, Object> fixture = new HashMap<>();
        fixture.put("iri", "http://opendata.inrae.fr/thesaurusINRAE/gr_6c79e7c5");
        fixture.put("backend_type", "ontoportal");
        fixture.put("short_form", "gr_6c79e7c5");
        fixture.put("label", "coll DEFINED CONCEPTS");
        fixture.put("source", "https://data.agroportal.lirmm.fr");
        fixture.put("source_name", "agroportal");
        fixture.put("ontology", "https://data.agroportal.lirmm.fr/ontologies/INRAETHES");
        fixture.put("descriptions", Collections.emptyList());
        fixture.put("synonyms", Collections.emptyList());
        fixture.put("type", "http://www.w3.org/2004/02/skos/core#Collection");
        fixture.put("created", null);
        fixture.put("modified", null);
        fixture.put("obsolete", false);
        fixture.put("source_url", null);
        fixture.put("version", null);
        fixture.put("ontology_iri", null); //TODO: fix this
        return fixture;
    }

    private Map<String, Object> createOlsFoodOnInstanceFixture() {
        Map<String, Object> fixture = createOls2FoodOnInstanceFixture();
        fixture.put("backend_type", "ols");
        fixture.put("source_name", "zbmed");
        fixture.put("source", "https://semanticlookup.zbmed.de/ols/api");
        fixture.put("type", null); //TODO: fix this
        fixture.put("descriptions", List.of("LanguaL curation note: US FDA 1995 Code:  QR"));
        fixture.put("source_url", "https://semanticlookup.zbmed.de/ols/api/ontologies/foodon/individuals/http%253A%252F%252Fpurl.obolibrary.org%252Fobo%252FGAZ_00000464?lang=en");
        return fixture;
    }

    private Map<String, Object> createOls2FoodOnInstanceFixture() {
        Map<String, Object> fixture = createFoodOnInstanceFixture();
        fixture.put("backend_type", "ols2");
        fixture.put("source", "https://www.ebi.ac.uk/ols4/api/v2");
        fixture.put("source_name", "ebi");
        fixture.put("ontology", "foodon");
        fixture.put("type", "individual");
        fixture.put("descriptions", List.of("LanguaL curation note: US FDA 1995 Code:  QR"));
        fixture.put("ontology_iri", "http://purl.obolibrary.org/obo/foodon.owl");
        return fixture;
    }

    private Map<String, Object> createOntoPortalFoodOnInstanceFixture() {
        Map<String, Object> fixture = createFoodOnInstanceFixture();
        fixture.put("backend_type", "ontoportal");
        fixture.put("source", "https://data.agroportal.lirmm.fr");
        fixture.put("source_name", "agroportal");
        fixture.put("ontology", null); //TODO: fix this
        fixture.put("type", "http://www.w3.org/2002/07/owl#NamedIndividual"); //TODO: harmonize with ols types
        return fixture;
    }

    private Map<String, Object> createFoodOnInstanceFixture() {
        Map<String, Object> fixture = new HashMap<>();
        fixture.put("short_form", "GAZ_00000464");
        fixture.put("iri", "http://purl.obolibrary.org/obo/GAZ_00000464");
        fixture.put("label", "Europe");
        fixture.put("source", "https://data.agroportal.lirmm.fr");
        fixture.put("ontology", "foodon");
        fixture.put("descriptions", Collections.emptyList());
        fixture.put("synonyms", Collections.emptyList());
        fixture.put("created", null);
        fixture.put("modified", null);
        fixture.put("obsolete", false);
        fixture.put("source_url", null); //TODO: ensure this is not null for ontoportal and ols v1
        fixture.put("version", null);
        fixture.put("ontology_iri", null); //TODO: ensure this is not null for ontoportal and ols v1
        return fixture;
    }

    private Map<String, Object> createOntoPortalInraeScheme() {
        Map<String, Object> fixture = new HashMap<>();
        fixture.put("label", "AGR hunting and fishing");
        fixture.put("iri", "http://opendata.inrae.fr/thesaurusINRAE/mt_50");
        fixture.put("backend_type", "ontoportal");
        fixture.put("synonyms", Collections.emptyList());
        fixture.put("created", null);
        fixture.put("obsolete", false);
        fixture.put("source", "https://data.agroportal.lirmm.fr");
        fixture.put("type", "http://www.w3.org/2004/02/skos/core#ConceptScheme");
        fixture.put("descriptions", Collections.emptyList());
        fixture.put("version", null);
        fixture.put("source_url", null);
        fixture.put("short_form", "mt_50");
        fixture.put("modified", null);
        fixture.put("ontology_iri", null);
        fixture.put("source_name", "agroportal");
        fixture.put("ontology", "https://data.agroportal.lirmm.fr/ontologies/INRAETHES");
        return fixture;
    }

    private Map<String, Object> createGndTermFixture() {
        Map<String, Object> fixture = new HashMap<>();
        fixture.put("iri", "https://d-nb.info/gnd/4074335-4");
        fixture.put("backend_type", "gnd");
        fixture.put("created", null);
        fixture.put("obsolete", false);
        fixture.put("source", "https://lobid.org");
        fixture.put("label", "London");
        fixture.put("type", "AuthorityResource");
        fixture.put("descriptions", List.of("Hauptstadt des Vereinigten Königreichs von Großbritannien und Nordirland, in Mittelsteinzeit besiedelt, 43 n. Chr. von Römern gegründet; das County of London war 1889-1965 Verwaltungsgrafschaft u. zeremonielle Grafschaft"));
        fixture.put("version", null);
        fixture.put("source_url", "https://d-nb.info/gnd/4074335-4");
        fixture.put("short_form", "4074335-4");
        fixture.put("modified", null);
        fixture.put("ontology_iri", null);
        fixture.put("source_name", "gnd");
        fixture.put("ontology", null);
        fixture.put("synonyms", List.of("Londen",
                "Corporation of London",
                "Augusta Trinobantum",
                "Landan",
                "Londres",
                "Londinum",
                "County of London",
                "Lundonia",
                "Londra",
                "Londyn",
                "Greater London",
                "London (Great Britain)",
                "Londinium",
                "Westminster",
                "Lundun"));

        return fixture;
    }

}
