package org.semantics.apigateway;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.mockito.Mock;
import org.semantics.apigateway.config.SourceConfig;
import org.semantics.apigateway.model.responses.AggregatedApiResponse;
import org.semantics.apigateway.model.responses.AggregatedResourceBody;
import org.semantics.apigateway.service.ApiAccessor;
import org.semantics.apigateway.service.JsonLdTransform;
import org.semantics.apigateway.service.configuration.ConfigurationLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Type;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.semantics.apigateway.service.JsonLdTransform.DEFAULT_BASE_URI;

public abstract class ApplicationTestAbstract {
    private final Logger logger = LoggerFactory.getLogger(SearchServiceTest.class);

    public JsonLdTransform jsonLdTransform = new JsonLdTransform();

    @Mock
    protected RestTemplate restTemplate; // Mock RestTemplate

    @Autowired
    protected ConfigurationLoader configurationLoader; // Autowire the real ConfigurationLoader

    protected ApiAccessor apiAccessor;

    protected Map<String, String> mockResponses = new HashMap<>();

    protected List<SourceConfig> configs;
    protected Class<? extends AggregatedResourceBody> responseClass;

    protected Map<String, String> readMockedResponses(String key, List<SourceConfig> configs) {
        Map<String, String> mockResponses = new HashMap<>();
        for (SourceConfig config : configs) {
            String serviceName = String.format("src/test/resources/mocks/" + key + "/%s.json", config.getName());
            String jsonResponse = "";
            try {
                jsonResponse = new String(Files.readAllBytes(Paths.get(serviceName)));
                mockResponses.put(config.getName(), jsonResponse);
                logger.info("Mocking file: {}", serviceName);
            } catch (Exception e) {
                logger.info("File: {} not found so not mocking", serviceName);
            }
        }
        return mockResponses;
    }

    /**
     * Create fixture for the AGROVOC ontoportal artefact
     */
    protected Map<String, Object> createOntoportalAgrovocFixture() {
        Map<String, Object> fixture = new HashMap<>();
        //TODO: add source_url
        //TODO: add @type
        //TODO: add @context
        //TODO: add @id
        //TODO: add other fields

        fixture.put("iri", "http://aims.fao.org/aos/agrovoc/");
        fixture.put("backend_type", "ontoportal");
        fixture.put("short_form", "AGROVOC");
        fixture.put("label", "AGROVOC");
        fixture.put("source", "https://data.agroportal.lirmm.fr");
        fixture.put("source_name", "agroportal");
        fixture.put("descriptions", List.of(
                "AGROVOC is a multilingual and controlled vocabulary designed to cover concepts and terminology under FAO's areas of interest. It is a large Linked Open Data set about agriculture, available for public use, and its highest impact is through facilitating the access and visibility of data across domains and languages."
        ));
        fixture.put("source_url", "http://agroportal.lirmm.fr/ontologies/AGROVOC");
        fixture.put("type", "http://data.bioontology.org/metadata/OntologySubmission");
        fixture.put("version", "2024-04");
        fixture.put("publisher", List.of("Food and Agriculture Organization of the United Nations"));
        fixture.put("modified", "2024-08-12T15:27:42.000+00:00");
        fixture.put("status", "production");
        fixture.put("accessRights", "public");
        fixture.put("accrualMethod", "http://www.fao.org/agrovoc/maintenance");
        fixture.put("accrualPeriodicity", "http://purl.org/cld/freq/monthly");
        fixture.put("rightsHolder", Collections.emptyList());

        // Added properties from the first code block
        fixture.put("createdWith", List.of("VocBench"));
        fixture.put("keywords", Collections.emptyList());
        fixture.put("contactPoint", List.of("AGROVOC@fao.org"));
        fixture.put("subject", List.of(
                " http://dbpedia.org/resource/Agriculture",
                "http://aims.fao.org/aos/agrovoc/c_203",
                "http://aims.fao.org/aos/agrovoc/c_2593",
                "http://aims.fao.org/aos/agrovoc/c_2934",
                "http://aims.fao.org/aos/agrovoc/c_3055",
                "http://aims.fao.org/aos/agrovoc/c_49892",
                "http://dbpedia.org/resource/Environment",
                "http://dbpedia.org/resource/Fishery",
                "http://dbpedia.org/resource/Food",
                "http://dbpedia.org/resource/Forestry",
                "http://dbpedia.org/resource/Nutrition"
        ));
        fixture.put("obsolete", false);
        fixture.put("language", List.of(
                "http://lexvo.org/id/iso639-1/ro",
                "http://lexvo.org/id/iso639-1/ar",
                "http://lexvo.org/id/iso639-1/ca",
                "http://lexvo.org/id/iso639-1/cs",
                "http://lexvo.org/id/iso639-1/da",
                "http://lexvo.org/id/iso639-1/de",
                "http://lexvo.org/id/iso639-1/el",
                "http://lexvo.org/id/iso639-1/en",
                "http://lexvo.org/id/iso639-1/es",
                "http://lexvo.org/id/iso639-1/et",
                "http://lexvo.org/id/iso639-1/fa",
                "http://lexvo.org/id/iso639-1/fi",
                "http://lexvo.org/id/iso639-1/fr",
                "http://lexvo.org/id/iso639-1/hi",
                "http://lexvo.org/id/iso639-1/hu",
                "http://lexvo.org/id/iso639-1/it",
                "http://lexvo.org/id/iso639-1/ja",
                "http://lexvo.org/id/iso639-1/ka",
                "http://lexvo.org/id/iso639-1/ko",
                "http://lexvo.org/id/iso639-1/lo",
                "http://lexvo.org/id/iso639-1/ms",
                "http://lexvo.org/id/iso639-1/nl",
                "http://lexvo.org/id/iso639-1/no",
                "http://lexvo.org/id/iso639-1/pl",
                "http://lexvo.org/id/iso639-1/pt",
                "http://lexvo.org/id/iso639-1/ru",
                "http://lexvo.org/id/iso639-1/sk",
                "http://lexvo.org/id/iso639-1/sr",
                "http://lexvo.org/id/iso639-1/sv",
                "http://lexvo.org/id/iso639-1/sw",
                "http://lexvo.org/id/iso639-1/te",
                "http://lexvo.org/id/iso639-1/th",
                "http://lexvo.org/id/iso639-1/tr",
                "http://lexvo.org/id/iso639-1/uk",
                "http://lexvo.org/id/iso639-1/zh"
        ));
        fixture.put("wasGeneratedBy", Collections.emptyList());
        fixture.put("contributor", Collections.emptyList());
        fixture.put("semanticArtefactRelation", null);
        fixture.put("versionIRI", null);
        fixture.put("identifier", null);
        fixture.put("hasFormat", Collections.emptyList());
        fixture.put("competencyQuestion", null);
        fixture.put("creator", List.of(
                "https://www.wikidata.org/entity/Q82151",
                "Food and Agriculture Organization of the United Nations",
                "Food and Agriculture Organization of the United Nations"
        ));
        fixture.put("synonyms", List.of("AGROVOC Dataset"));
        fixture.put("bibliographicCitation", List.of("https://www.fao.org/agrovoc/publications"));
        fixture.put("coverage", "FAO's areas of interest");
        fixture.put("created", "2025-03-04T18:00:17.000+01:00");
        fixture.put("landingPage", "http://www.fao.org/agrovoc/");
        fixture.put("license", "https://creativecommons.org/licenses/by/4.0/");
        fixture.put("includedInDataCatalog", List.of(
                "https://bartoc.org/en/node/305",
                "https://vocabs.ardc.edu.au/viewById/2",
                "http://agroportal.lirmm.fr/ontologies/AGROVOC",
                "https://agroportal.lirmm.fr/",
                "https://fairsharing.org/FAIRsharing.anpj91",
                "https://lod-cloud.net/dataset/agrovoc"
        ));
        return fixture;
    }

    /**
     * Create fixture for the AGROVOC skosmos artefact
     */
    protected Map<String, Object> createSkosmosAgrovocFixture() {
        Map<String, Object> fixture = new HashMap<>();
        fixture.put("iri", "http://aims.fao.org/aos/agrovoc");
        fixture.put("backend_type", "skosmos");
        fixture.put("label", "agrovoc");
        fixture.put("short_form", "agrovoc");
        fixture.put("source", "https://agrovoc.fao.org/browse/rest/v1");
        fixture.put("source_name", "agrovoc");
        fixture.put("descriptions", List.of("AGROVOC Multilingual Thesaurus"));
        fixture.put("source_url", null);
        fixture.put("type", null);
        fixture.put("landingPage", null);
        fixture.put("version", null);
        fixture.put("license", null);
        fixture.put("publisher", null);
        fixture.put("created", null);
        fixture.put("modified", null);
        fixture.put("status", null);
        fixture.put("accessRights", null);
        fixture.put("accrualMethod", null);
        fixture.put("accrualPeriodicity", null);
        fixture.put("coverage", null);
        fixture.put("rightsHolder", null);
        fixture.put("createdWith", null);
        fixture.put("keywords", null);
        fixture.put("contactPoint", null);
        fixture.put("subject", null);
        fixture.put("obsolete", false);
        fixture.put("language", null);
        fixture.put("wasGeneratedBy", null);
        fixture.put("contributor", null);
        fixture.put("semanticArtefactRelation", null);
        fixture.put("versionIRI", null);
        fixture.put("identifier", null);
        fixture.put("hasFormat", null);
        fixture.put("competencyQuestion", null);
        fixture.put("creator", null);
        fixture.put("synonyms", null);
        fixture.put("includedInDataCatalog", null);
        fixture.put("bibliographicCitation", null);

        return fixture;
    }

    /**
     * Create fixture for the AGROVOC ols artefact
     */
    protected Map<String, Object> createOlsAgrovocFixture() {
        Map<String, Object> fixture = new HashMap<>();
        fixture.put("label", "AGROVOC Multilingual Thesaurus");
        fixture.put("short_form", "agrovoc");
        fixture.put("source", "https://semanticlookup.zbmed.de/ols/api");
        fixture.put("source_name", "zbmed");
        fixture.put("iri", "http://aims.fao.org/aos/agrovoc/");
        fixture.put("backend_type", "ols");
        fixture.put("descriptions", List.of(
                "AGROVOC is a multilingual and controlled vocabulary designed to cover concepts and terminology under FAO's areas of interest. It is the largest Linked Open Data set about agriculture available for public use and its greatest impact is through providing the access and visibility of data across domains and languages.\nIt offers a structured collection of agricultural concepts, terms, definitions and relationships which are used to unambiguously identify resources, allowing standardized indexing processes and making searches more efficient. The thesaurus is hierarchically organized under 25 top concepts.\nAGROVOC uses semantic web technologies, linking to other multilingual knowledge organization systems and building bridges between datasets. AGROVOC is edited using VocBench 3, a web-based vocabulary management tool.\nFor more information, please consult http://www.fao.org/agrovoc/.\nThe designations employed and the presentation of material in this information product do not imply the expression of any opinion whatsoever on the part of the Food and Agriculture Organization of the United Nations (FAO) concerning the legal or development status of any country, territory, city or area or of its authorities, or concerning the delimitation of its frontiers or boundaries."
        ));
        fixture.put("source_url", "http://semanticlookup.zbmed.de/ols/api/ontologies/agrovoc");
        fixture.put("type", null);
        fixture.put("landingPage", null);
        fixture.put("version", null);
        fixture.put("license", null);
        fixture.put("publisher", null);
        fixture.put("created", null);
        fixture.put("modified", null);
        fixture.put("status", null);
        fixture.put("accessRights", null);
        fixture.put("accrualMethod", null);
        fixture.put("accrualPeriodicity", null);
        fixture.put("coverage", null);
        fixture.put("rightsHolder", null);
        fixture.put("createdWith", null);
        fixture.put("keywords", null);
        fixture.put("contactPoint", null);
        fixture.put("subject", null);
        fixture.put("obsolete", false);
        fixture.put("language", null);
        fixture.put("wasGeneratedBy", null);
        fixture.put("contributor", null);
        fixture.put("semanticArtefactRelation", null);
        fixture.put("versionIRI", null);
        fixture.put("identifier", null);
        fixture.put("hasFormat", null);
        fixture.put("competencyQuestion", null);
        fixture.put("creator", null);
        fixture.put("synonyms", Collections.emptyList());
        fixture.put("includedInDataCatalog", null);
        fixture.put("bibliographicCitation", null);

        return fixture;
    }

    protected void mockApiAccessor(String key, ApiAccessor apiAccessor) {
        this.apiAccessor = apiAccessor;
        apiAccessor.setRestTemplate(restTemplate);
        this.configs = configurationLoader.getSourceConfigs();
        this.mockResponses = this.readMockedResponses(key, configs);
        when(restTemplate.getForEntity(
                anyString(),
                eq(Object.class)))
                .thenAnswer(invocation -> {
                    String url = invocation.getArgument(0, String.class);
                    ResponseEntity<Map<String, Object>> response = ResponseEntity.status(404).body(new HashMap<>());
                    for (SourceConfig config : configs) {
                        String configHost = new URL(config.getUrl()).getHost();
                        String currentURLHost = new URL(url).getHost();


                        if (configHost.equals(currentURLHost)) {
                            String jsonResponse = mockResponses.get(config.getName());
                            Gson gson = new Gson();
                            Type mapType = new TypeToken<Object>() {
                            }.getType();
                            Object map = gson.fromJson(jsonResponse, mapType);

                            if (map instanceof List) {
                                Map<String, Object> out = new HashMap<>();
                                out.put("collection", map);
                                response = ResponseEntity.status(200).body(out);
                            } else {
                                response = ResponseEntity.status(200).body((Map<String, Object>) map);
                            }


                            return response;
                        }
                    }
                    return response;
                });
    }


    protected Map<String, Object> findByIriAndBackendType(List<Map<String, Object>> responseList, String iri, String backendType) {
        return responseList.stream().filter(x -> x.get("iri").equals(iri) && x.get("backend_type").equals(backendType)).findFirst().orElse(null);
    }

    protected int indexOfIriAndBackendType(List<Map<String, Object>> responseList, String iri, String backendType) {
        return IntStream.range(0, responseList.size())
                .filter(i -> responseList.get(i).get("iri").equals(iri) && responseList.get(i).get("backend_type").equals(backendType))
                .findFirst()
                .orElse(-1);
    }

    protected Map<String, Object> findByShortFormAndBackendType(List<Map<String, Object>> responseList, String shortForm, String backendType) {
        return responseList.stream().filter(x -> x.get("short_form").equals(shortForm) && x.get("backend_type").equals(backendType)).findFirst().orElse(null);
    }

    protected int indexOfShortFormAndBackendType(List<Map<String, Object>> responseList, String shortForm, String backendType) {
        return IntStream.range(0, responseList.size())
                .filter(i -> responseList.get(i).get("short_form").equals(shortForm) && responseList.get(i).get("backend_type").equals(backendType))
                .findFirst()
                .orElse(-1);
    }

    protected void assertMapEquality(AggregatedApiResponse actual, Map<String, Object> expected) {
        assertMapEquality(actual, expected, 1, 0);

    }

    protected void assertMapEquality(AggregatedApiResponse actual, Map<String, Object> expected, int size) {
        assertMapEquality(actual, expected, size, 0);

    }

    protected void assertMapEquality(AggregatedApiResponse actual, Map<String, Object> expected, int size, int index) {
        assertThat(actual).isNotNull();
        List<Map<String, Object>> expectedList = actual.getCollection();
        assertThat(expectedList).hasSize(size);
        Map<String, Object> firstPlant = expectedList.get(index);
        assertThat(firstPlant).containsAllEntriesOf(expected);

        String type = null;
        try {
            type = responseClass.getDeclaredConstructor().newInstance().getTypeURI();
        } catch (Exception ignored) {
        }
        assertValidJsonLd(firstPlant, type);
    }

    protected void assertValidJsonLd(Map<String, Object> firstPlant, String type) {
        assertThat(firstPlant.get("@type")).isNotNull().isNotEqualTo("")
                .isEqualTo(type);
        assertThat(firstPlant.get("@id")).isEqualTo(firstPlant.get("iri"));
        assertThat(firstPlant.get("@context")).isNotNull().isNotEqualTo(Collections.emptyMap());
        Map<String, Object> context = (Map<String, Object>) firstPlant.get("@context");
        List<String> keysNotInContext = List.of(new String[]{"@type", "@id", "@context"});
        Set<String> keys = firstPlant.keySet().stream()
                .filter(x -> !keysNotInContext.contains(x)).collect(Collectors.toSet());
        keys.add("@base");

        assertThat(context.keySet().stream().sorted()).isEqualTo(keys.stream().sorted().toList()).isNotEmpty();
        String defaultBaseUri = DEFAULT_BASE_URI;
        String base = jsonLdTransform.getBaseUri();
        Map<String, String> namespaces = jsonLdTransform.getNameSpaceMap();
        assertThat(context.get("@base")).isEqualTo(base).isNotNull();
        assertThat(context.get("iri")).isEqualTo(defaultBaseUri + "iri");
        assertThat(context.get("backend_type")).isEqualTo(defaultBaseUri + "backend_type");
        assertThat(context.get("short_form")).isEqualTo(namespaces.get("skos") + "notation");
        assertThat(context.get("label")).isEqualTo(namespaces.get("skos") + "prefLabel");
        assertThat(context.get("created")).isEqualTo(namespaces.get("dct") + "created");
    }

    protected Map<String, Object> createGndFixture() {
        Map<String, Object> fixture = new HashMap<>();
        fixture.put("iri", "https://lobid.org/gnd");
        fixture.put("backend_type", "gnd");
        fixture.put("short_form", "GND");
        fixture.put("label", "GDN");
        fixture.put("source", "https://lobid.org");
        fixture.put("source_name", "gnd");
        fixture.put("source_url", "https://lobid.org/gnd");
        fixture.put("descriptions", List.of("The Common Authority File (GND) contains more than 8 million standard data sets. It is used to catalog literature in libraries, as well as archives, museums and research projects."));
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
        fixture.put("hasFormat", List.of("json", "ttl", "rdf/xml"));
        fixture.put("competencyQuestion", null);
        fixture.put("semanticArtefactRelation", null);
        fixture.put("wasGeneratedBy", null);
        fixture.put("includedInDataCatalog", null);
        fixture.put("accrualMethod", null);
        fixture.put("accrualPeriodicity", null);
        fixture.put("bibliographicCitation", null);
        fixture.put("contactPoint", List.of("https://lobid.org/team"));
        fixture.put("subject", null);
        fixture.put("type", "http://www.w3.org/2002/07/owl#Ontology");
        fixture.put("modified", "Updated hourly");
        return fixture;
    }

    protected Map<String, Object> createColiConc(){
        Map<String, Object> fixture = new HashMap<>();
        fixture.put("iri", "http://bartoc.org/en/node/15");
        fixture.put("source_url", null);
        fixture.put("backend_type", "jskos2");
        fixture.put("short_form", "EuroVoc");
        fixture.put("label", "Multilingual Thesaurus of the European Union");
        fixture.put("source", "https://coli-conc.gbv.de/api");
        fixture.put("source_name", "coli-conc");
        fixture.put("descriptions", List.of("\"EuroVoc is a multilingual, multidisciplinary thesaurus covering the activities of the EU, the European Parliament in particular. It contains terms in 23 EU languages (Bulgarian, Croatian, Czech, Danish, Dutch, English, Estonian, Finnish, French, German, Greek, Hungarian, Italian, Latvian, Lithuanian, Maltese, Polish, Portuguese, Romanian, Slovak, Slovenian, Spanish and Swedish), plus in three languages of countries which are candidates for EU accession: македонски (mk), shqip (sq) and cрпски (sr).\n"+
"\n"+
"It is a multi-disciplinary thesaurus covering fields which are sufficiently wide-ranging to encompass both Community and national points of view, with a certain emphasis on parliamentary activities. EuroVoc is a controlled set of vocabulary which can be used outside the EU institutions, particularly by parliaments.\n"+
                "\n"+
"The aim of the thesaurus is to provide the information management and dissemination services with a coherent indexing tool for the effective management of their documentary resources and to enable users to carry out documentary searches using controlled vocabulary.\""));
        fixture.put("type", "http://www.w3.org/2004/02/skos/core#ConceptScheme");
        fixture.put("version", null);
        fixture.put("publisher", List.of("Publications Office of the European Union"));
        fixture.put("modified", "2023-09-12T09:18:03.554Z");
        fixture.put("status", null);
        fixture.put("accessRights", null);
        fixture.put("accrualMethod", null);
        fixture.put("accrualPeriodicity", null);
        fixture.put("rightsHolder", null);
        fixture.put("createdWith", null);
        fixture.put("keywords", null);
        fixture.put("contactPoint", null);
        fixture.put("subject", List.of(
                "http://dewey.info/class/0/e23/",
                "http://dewey.info/class/001/e23/",
                "http://eurovoc.europa.eu/4704",
                "http://eurovoc.europa.eu/1172",
                "http://eurovoc.europa.eu/77",
                "http://eurovoc.europa.eu/6894",
                "http://www.iskoi.org/ilc/2/class/V",
                "http://www.iskoi.org/ilc/2/class/tue"
        ));
        fixture.put("obsolete", false);
        fixture.put("language", List.of(
                "bg",
                "ca",
                "hr",
                "cs",
                "da",
                "nl",
                "en",
                "et",
                "fi",
                "fr",
                "de",
                "el",
                "hu",
                "it",
                "lv",
                "lt",
                "mk",
                "mt",
                "pl",
                "pt",
                "ro",
                "sr",
                "sk",
                "sl",
                "es",
                "sv"
        ));
        fixture.put("wasGeneratedBy", null);
        fixture.put("contributor", List.of(
                "Mpaunescu",
                "Sabrina Gaab",
                "David-Benjamin Rohrer",
                "JakobVoss"
        ));
        fixture.put("semanticArtefactRelation", null);
        fixture.put("versionIRI", null);
        fixture.put("identifier", "http://publications.europa.eu/resource/dataset/eurovoc");
        fixture.put("hasFormat", List.of(
                "http://bartoc.org/en/Format/Online",
                "http://bartoc.org/en/Format/PDF",
                "http://bartoc.org/en/Format/SKOS",
                "http://bartoc.org/en/Format/XML",
                "http://bartoc.org/en/Format/RDF",
                "http://bartoc.org/en/Format/Spreadsheet",
                "http://bartoc.org/en/Format/XSD",
                "http://bartoc.org/en/Format/Database"
        ));
        fixture.put("competencyQuestion", null);
        fixture.put("creator", null);
        fixture.put("synonyms", null);
        fixture.put("created", "2013-08-14T14:05:00Z");
        fixture.put("landingPage", null);
        fixture.put("license", "http://creativecommons.org/publicdomain/zero/1.0/");
        fixture.put("includedInDataCatalog", List.of(
                "https://skosmos.bartoc.org/15/",
                "https://publications.europa.eu/webapi/rdf/sparql"
        ));

        return fixture;
    }
    protected Map<String, Object> createDanteFixture() {
        Map<String, Object> fixture = new HashMap<>();
        fixture.put("backend_type", "jskos");
        fixture.put("keywords", null);
        fixture.put("contactPoint", null);
        fixture.put("language", List.of("de", "en"));
        fixture.put("source", "https://api.dante.gbv.de");
        fixture.put("type", "http://www.w3.org/2004/02/skos/core#ConceptScheme");
        fixture.put("descriptions", List.of("Eine Liste von Geschlechtern"));
        fixture.put("source_url", null);
        fixture.put("modified", "2022-11-21");
        fixture.put("source_name", "dante");
        fixture.put("iri", "http://uri.gbv.de/terminology/gender/");
        fixture.put("identifier", null);
        fixture.put("hasFormat", null);
        fixture.put("creator", null);
        fixture.put("synonyms", null);
        fixture.put("created", "2016-12-13");
        fixture.put("landingPage", null);
        fixture.put("label", "Gender");
        fixture.put("version", null);
        fixture.put("license", null);
        fixture.put("short_form", "gender");
        fixture.put("publisher", Collections.singletonList("Verbundzentrale des GBV (VZG)"));
        fixture.put("accessRights", null);
        fixture.put("status", null);


        return fixture;
    }
}
