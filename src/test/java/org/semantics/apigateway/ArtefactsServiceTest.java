package org.semantics.apigateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.semantics.apigateway.model.CommonRequestParams;
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
    }

    @Test
    public void testGetAllArtefacts() {
        AggregatedApiResponse response = (AggregatedApiResponse) artefactsService.getArtefacts(new CommonRequestParams(), null, null, apiAccessor);
        List<Map<String, Object>> responseList = response.getCollection();

        assertThat(responseList.size()).isEqualTo(724);

        Map<String, Object> ontoportalItem = findByShortFormAndBackendType(responseList, "AGROVOC", "ontoportal");
        assertThat(ontoportalItem).containsAllEntriesOf(createOntoportalAgrovocFixture());

        Map<String, Object> skosmosItem = findByShortFormAndBackendType(responseList, "agrovoc", "skosmos");
        Map<String, Object> skosmosExpected = createSkosmosAgrovocFixture();
        skosmosExpected.put("iri", "agrovoc");
        assertThat(skosmosItem).containsAllEntriesOf(skosmosExpected);

        Map<String, Object> olsItem = findByShortFormAndBackendType(responseList, "bto", "ols");
        assertThat(olsItem).containsAllEntriesOf(createOlsFixture());

        Map<String, Object> ols2Item = findByShortFormAndBackendType(responseList, "bto", "ols2");
        assertThat(ols2Item).containsAllEntriesOf(createOls2Fixture());

        Map<String, Object> gndItem = findByShortFormAndBackendType(responseList, "GND", "gnd");

        assertThat(gndItem).containsAllEntriesOf(createGndFixture());

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
        fixture.put("ontology", "bto");
        fixture.put("synonyms", Collections.emptyList());
        fixture.put("created", null);
        fixture.put("obsolete", false);
        fixture.put("source_url", "https://service.tib.eu:443/ts4tib/api/ontologies/bto");
        fixture.put("modified", null);
        fixture.put("ontology_iri", null);
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
        fixture.put("ontology", "bto");
        fixture.put("synonyms", Collections.emptyList());
        fixture.put("created", null);
        fixture.put("obsolete", false);
        fixture.put("source_url", null);
        fixture.put("modified", "2025-03-16T21:34:29.847078927");
        fixture.put("ontology_iri", "http://purl.obolibrary.org/obo/bto.owl");
        fixture.put("version", null);
        fixture.put("descriptions", List.of(
                "A structured controlled vocabulary for the source of an enzyme comprising tissues, cell lines, cell types and cell cultures."
        ));
        return fixture;
    }

    private Map<String,Object> createGndFixture(){
        //{rightsHolder=null, backend_type=gnd, createdWith=null, keywords=[authority data, Germany, Austria, Switzerland], contactPoint=[https://lobid.org/team], subject=null, obsolete=false, language=[de], source=https://lobid.org, type=http://www.w3.org/2002/07/owl#Ontology, descriptions=[The Common Authority File (GND) contains more than 8 million standard data sets. It is used to catalog literature in libraries, as well as archives, museums and research projects.], source_url=https://lobid.org/gnd, accrualMethod=null, wasGeneratedBy=null, contributor=null, semanticArtefactRelation=null, modified=Updated hourly, ontology_iri=null, source_name=gnd, ontology=null, versionIRI=null, coverage=null, iri=https://lobid.org/gnd, identifier=https://lobid.org/gnd, hasFormat=json;ttl;rdf/xml, competencyQuestion=null, creator=[Hochschulbibliothekszentrum des Landes Nordrhein-Westfalen (hbz)], synonyms=[lobid GND], created=2018-07-11, landingPage=https://lobid.org/gnd, label=GDN, version=Not specified, license=CC0 1.0, includedInDataCatalog=null, short_form=GND, publisher=[Hochschulbibliothekszentrum des Landes Nordrhein-Westfalen (hbz)], accrualPeriodicity=null, accessRights=public, bibliographicCitation=null, status=production}
        Map<String, Object> fixture = new HashMap<>();
        fixture.put("iri", "https://lobid.org/gnd");
        fixture.put("backend_type", "gnd");
        fixture.put("short_form", "GND");
        fixture.put("label", "GDN");
        fixture.put("source", "https://lobid.org");
        fixture.put("source_name", "gnd");
        fixture.put("source_url", "https://lobid.org/gnd");
        fixture.put("descriptions", List.of("The Common Authority File (GND) contains more than 8 million standard data sets. It is used to catalog literature in libraries, as well as archives, museums and research projects."));
        fixture.put("ontology", null);
        fixture.put("ontology_iri", null);
        fixture.put("synonyms", List.of("lobid GND"));
        fixture.put("created", "2018-07-11");
        fixture.put("obsolete", false);
        fixture.put("version", "Not specified");
        fixture.put("status", "production");
        fixture.put("versionIRI", null);
        fixture.put("accessRights", "public");
        fixture.put("license", "CC0 1.0");
        fixture.put("identifier", "https://lobid.org/gnd");
        fixture.put("keywords", List.of("authority data", "Germany", "Austria", "Switzerland"));
        fixture.put("landingPage", "https://lobid.org/gnd");
        fixture.put("language", List.of("de"));
        fixture.put("creator", List.of("Hochschulbibliothekszentrum des Landes Nordrhein-Westfalen (hbz)"));
        fixture.put("publisher", List.of("Hochschulbibliothekszentrum des Landes Nordrhein-Westfalen (hbz)"));
        fixture.put("createdWith", null);
        fixture.put("contributor", null);
        fixture.put("rightsHolder", null);
        fixture.put("coverage", null);
        fixture.put("hasFormat", "json;ttl;rdf/xml");
        fixture.put("competencyQuestion", null);
        fixture.put("semanticArtefactRelation", null);
        fixture.put("wasGeneratedBy", null);
        fixture.put("includedInDataCatalog", null);
        fixture.put("accrualMethod", null);
        fixture.put("accrualPeriodicity", null);
        fixture.put("bibliographicCitation", null);
        fixture.put("contactPoint", List.of("https://lobid.org/team"));
        fixture.put("subject", null);
        return fixture;
    }
}
