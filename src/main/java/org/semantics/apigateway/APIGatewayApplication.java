package org.semantics.apigateway;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.semantics.apigateway.model.user.Role;
import org.semantics.apigateway.model.user.User;
import org.semantics.apigateway.service.auth.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.concurrent.Executor;

@SpringBootApplication
@EnableConfigurationProperties
@EnableAsync
@EnableCaching
@OpenAPIDefinition(
        info = @Info(title = "API Gateway Documentation", version = "1.0", description = "The TS4NFDI API Gateway lets you query many Terminology Services (TS) within the NFDI ecosystem and beyond " +
                "(e.g. OLS instances, OntoPortal instances such as AgroPortal or BiodivPortal, Skosmos instances, GND and others) " +
                "through **one unified API**, instead of calling the API of each service separately. " +
                "The gateway forwards your request to the connected services in parallel, translates their different answers into one common format, and returns one combined result.\n" +
                "\n" +
                "**Use this gateway to:**\n" +
                "\n" +
                "- **Search** for terms (e.g. classes and concepts) in all connected terminology services at once (see **MOD - Search**).\n" +
                "- **List and inspect artefacts**, i.e. ontologies, thesauri and vocabularies: title, description, version, and which service hosts them (see **MOD - Artefacts**).\n" +
                "- **Browse the content of one artefact**: its classes/concepts, properties, individuals, concept schemes, SKOS collections, and the class/concept hierarchy (see **MOD - Artefact Data**).\n" +
                "- **Keep using existing OLS clients:** the OLS-compatible endpoints accept OLS-style requests and answer in OLS format, " +
                "but the data comes from all connected services, not only from one OLS instance (see **OLS4 v2**, **OLS4**).\n" +
                "- **Create terminology collections**, i.e. named, shareable sets of terminologies, and use them to restrict other requests to these terminologies " +
                "(see **Collections**, **Users**, **Authentication**).\n" +
                "- **Check which terminology services are connected and reachable** (see **Configuration**, **Status**).\n" +
                "\n" +
                "Responses are JSON. They also carry JSON-LD annotations (`@context`, `@type`), so they can be used as plain data or as Linked Data.\n" +
                "\n" +
                "The gateway is driven by JSON configuration files that describe each connected terminology service and how its API maps to the unified endpoints of the gateway. " +
                "This means new terminology services can be added, or existing ones reconfigured, without changing the API that client applications rely on.\n" +
                "\n" +
                "**Where to start:**\n" +
                "\n" +
                "- New to the gateway? Try `GET /search?query=plant` to see combined results from all connected services.\n" +
                "- Looking for a specific ontology or vocabulary? Use `GET /artefacts` to list what is available, " +
                "then use the `short_form` of an artefact (e.g. `ncit`) as `{id}` in the `/artefacts/{id}/...` endpoints to explore its content.\n" +
                "- Want to know which services are connected? See `GET /config/databases`.\n" +
                "- Want to see working example requests? See `GET /status/examples`.\n" +
                "\n" +
                "The API of the gateway is based on the [MOD-API](https://github.com/FAIR-IMPACT/MOD-API) and uses its terminology. " +
                "The sections below explain this terminology and other terms used in this documentation.\n" +
                "\n" +
                "<details>\n" +
                "<summary><strong>Glossary: MOD, catalogue, semantic artefact</strong></summary>\n" +
                "\n" +
                "- **MOD (Metadata for Ontology Description and Publication):** a vocabulary for describing semantic artefacts. " +
                "MOD extends [DCAT](https://www.w3.org/TR/vocab-dcat/), the W3C vocabulary for describing catalogues and datasets. " +
                "The [MOD-API](https://github.com/FAIR-IMPACT/MOD-API) defines a common REST API for catalogues of semantic artefacts. " +
                "The endpoints in the groups starting with **MOD** follow this API.\n" +
                "- **Catalogue:** a service that publishes metadata records about semantic artefacts and their available resources or distributions. " +
                "The MOD-API offers operations for catalogue information, records, search, and service documentation. " +
                "The gateway itself acts as one catalogue that combines the catalogues of all connected terminology services (see `GET /`).\n" +
                "- **Semantic artefact (short: artefact):** an umbrella term for ontologies, terminologies, taxonomies, thesauri, vocabularies, metadata schemas, and semantic standards. " +
                "In this API, *artefact*, *terminology*, and *ontology* are used largely as synonyms.\n" +
                "- **Semantic resource:** a concrete, published or accessible representation of an artefact, e.g. an RDF download, an OWL file, a JSON-LD distribution, or an API endpoint. " +
                "Note that in the gateway paths, `/artefacts/{id}/resources/...` is used for the *content* of an artefact (its classes, concepts, properties, ...).\n" +
                "\n" +
                "**MOD endpoints vs. OLS endpoints:** the **MOD** groups are the native API of the gateway (paths `/search`, `/artefacts/...`) and use the MOD terminology. " +
                "The **OLS4 v2** and **OLS4** groups (paths `/ols/api/...`, `/ols4/api/...`) offer the same data with the paths, parameters, and response format of the " +
                "[Ontology Lookup Service (OLS)](https://www.ebi.ac.uk/ols4/), so that existing OLS clients can use the gateway without changes. " +
                "For new applications, the MOD endpoints are recommended.\n" +
                "\n" +
                "</details>\n" +
                "<details>\n" +
                "<summary><strong>Glossary: RDF, OWL, SKOS and the kinds of terms</strong></summary>\n" +
                "\n" +
                "Terminologies are usually published in one of the following W3C standards:\n" +
                "\n" +
                "- **[RDF](https://www.w3.org/RDF/) (Resource Description Framework):** a data model that represents information as *subject – predicate – object* statements, " +
                "e.g. \"Paris – is capital of – France\". OWL and SKOS both build on RDF.\n" +
                "- **[OWL](https://www.w3.org/TR/owl-ref/) (Web Ontology Language):** a formally defined language for *ontologies*, i.e. detailed, logical models of a subject area " +
                "with classes, properties, individuals, data values, and logical rules (axioms). Think of a smart, computer-readable encyclopedia structure.\n" +
                "- **[SKOS](https://www.w3.org/TR/skos-reference/) (Simple Knowledge Organization System):** a simpler model for controlled vocabularies, taxonomies, thesauri, " +
                "classification schemes, and tag/keyword systems. It organizes concepts and how they relate (broader, narrower, related terms).\n" +
                "\n" +
                "The gateway returns content of both OWL ontologies and SKOS vocabularies. Some words mean slightly different things in the two worlds:\n" +
                "\n" +
                "| Word | In OWL | In SKOS |\n" +
                "|------|--------|---------|\n" +
                "| **Term** | Any entity used to describe a domain of interest. The main OWL entities are classes, properties, and individuals. Each has a unique identifier (IRI). | A text label of a concept, e.g. its preferred label, alternative label, or hidden label. |\n" +
                "| **Class** | A category and, formally, a set of individuals that have something in common (`owl:Class`), e.g. \"Car\". | SKOS itself only uses a few fixed classes: `skos:Concept`, `skos:ConceptScheme` (a whole vocabulary), and `skos:Collection` (a group of concepts). |\n" +
                "| **Concept** | – | One entry of a vocabulary (`skos:Concept`), e.g. \"Renewable energy\". Concepts play a role similar to OWL classes. |\n" +
                "| **Property** | A relation or attribute. Object properties relate individuals to individuals, datatype properties relate individuals to data values (e.g. \"has colour\"). | A relationship between concepts, e.g. \"broader\", \"narrower\", or \"in scheme\". |\n" +
                "| **Individual** | A concrete member (instance) of a class, e.g. \"my car\" is an individual of the class \"Car\". | – (each concept is already a concrete entry) |\n" +
                "\n" +
                "In this API, *entity* is used as umbrella word for terms, classes, concepts, properties, and individuals.\n" +
                "\n" +
                "</details>\n" +
                "<details>\n" +
                "<summary><strong>Glossary: terminology collection, entity set, provider, and the three kinds of \"collection\"</strong></summary>\n" +
                "\n" +
                "- **Terminology collection:** a set of terminologies, created for a specific purpose or context in which this set is relevant, e.g. \"all terminologies relevant for plant research\". " +
                "A terminology collection can be shared with the community and can contain ontologies, thesauri, etc., without any rating. " +
                "It is a grouping within the vast space of existing terminologies. " +
                "Terminology collections are managed in the **Collections** group; pass the `id` of a terminology collection as `collectionId` to restrict other requests to its terminologies.\n" +
                "- **Entity set:** a flat, optionally ordered set of selected IRIs, created for a specific purpose or context. " +
                "It contains no semantic relations among its entities and can consist of entities from several terminologies and providers. " +
                "There are no dedicated entity set endpoints yet.\n" +
                "- **Provider (also, but deprecated: source, database):** an external terminology service or vocabulary provider that the gateway connects to, e.g. an OLS, OntoPortal, or Skosmos instance. " +
                "Each provider is an independent, third-party system that maintains its own terminologies and exposes them through its own API. " +
                "In parameters and responses, a provider is called `database` (e.g. the `database` parameter, `GET /config/databases`) or `source` (e.g. the `source_name` field). " +
                "The connected providers are listed in the next section.\n" +
                "\n" +
                "The word **collection** has three different meanings in this API:\n" +
                "\n" +
                "| Kind | Meaning | Where it appears |\n" +
                "|------|---------|------------------|\n" +
                "| **Terminology collection** (TS4NFDI) | A set of terminologies, see above. | The **Collections** group, the `collectionId` parameter. |\n" +
                "| **SKOS collection** (`skos:Collection`) | A labeled group of SKOS concepts within one vocabulary, used for organization or display. | `GET /artefacts/{id}/resources/collections` |\n" +
                "| **Response collection** | The list of result items in a response. | The `collection` field of the response body. |\n" +
                "\n" +
                "</details>\n" +
                "<details>\n" +
                "<summary><strong>Connected providers</strong></summary>\n" +
                "\n" +
                "The following providers are connected by default. The live list of the running gateway is returned by `GET /config/databases`. " +
                "Use the values of the *Name* or *Type* column in the `database` parameter.\n" +
                "\n" +
                "| Name | Type | Provider |\n" +
                "|------|------|--------|\n" +
                "| `agroportal` | `ontoportal` | [AgroPortal](https://agroportal.lirmm.fr) – agri-food |\n" +
                "| `earthportal` | `ontoportal` | [EarthPortal](https://earthportal.eu) – earth and environmental sciences |\n" +
                "| `biodivportal` | `ontoportal` | [BiodivPortal](https://biodivportal.gfbio.org) – biodiversity |\n" +
                "| `ecoportal` | `ontoportal` | [EcoPortal](https://ecoportal.lifewatch.eu) – ecology |\n" +
                "| `lovportal` | `ontoportal` | LOVPortal – Linked Open Vocabularies |\n" +
                "| `ebi` | `ols2` | [EMBL-EBI OLS4](https://www.ebi.ac.uk/ols4/) – life sciences |\n" +
                "| `tib` | `ols2` | [TIB Terminology Service](https://terminology.tib.eu) |\n" +
                "| `zbmed` | `ols2` | [ZB MED SemLookP](https://semanticlookup.zbmed.de) – life sciences |\n" +
                "| `berd` | `ols2` | [BERD@NFDI Terminology Service](https://ts.berd-nfdi.de) – business, economic and related data |\n" +
                "| `agrovoc` | `skosmos` | [AGROVOC](https://agrovoc.fao.org) (FAO) – agriculture |\n" +
                "| `zbmed-skosmos` | `skosmos` | [ZB MED Skosmos](https://semanticlookup.zbmed.de/skosmos/) |\n" +
                "| `gnd` | `gnd` | [GND](https://lobid.org/gnd) (Integrated Authority File, via lobid) |\n" +
                "| `dante` | `jskos` | [DANTE](https://dante.gbv.de) (GBV) |\n" +
                "| `coli-conc` | `jskos2` | [coli-conc](https://coli-conc.gbv.de) (GBV) |\n" +
                "| `iconclass` | `jskos2` | [Iconclass](https://iconclass.org) – art and iconography |\n" +
                "| `nerc` | `nerc` | [NERC Vocabulary Server](https://vocab.nerc.ac.uk) – marine and environmental sciences |\n" +
                "\n" +
                "</details>\n" +
                "<details>\n" +
                "<summary><strong>Identifiers: IRI, URI, URL, id</strong></summary>\n" +
                "\n" +
                "- **[URL](https://en.wikipedia.org/wiki/URL) (Uniform Resource Locator):** the address of a resource on the Web, e.g. `https://www.w3.org/RDF/`.\n" +
                "- **[URI](https://en.wikipedia.org/wiki/Uniform_Resource_Identifier) (Uniform Resource Identifier):** a unique sequence of characters that identifies an abstract or physical resource. " +
                "Every URL is a URI, but a URI does not have to be resolvable.\n" +
                "- **[IRI](https://en.wikipedia.org/wiki/Internationalized_Resource_Identifier) (Internationalized Resource Identifier):** a URI that may also contain non-ASCII characters. " +
                "Every URI is an IRI. In terminologies, classes, concepts, properties, and individuals are identified by IRIs, e.g. `http://purl.obolibrary.org/obo/NCIT_C2984`.\n" +
                "\n" +
                "So: URL ⊂ URI ⊂ IRI. In practice, the gateway treats *IRI* and *URI* as the same thing. They are used as follows:\n" +
                "\n" +
                "| Name | Where | Meaning |\n" +
                "|------|-------|---------|\n" +
                "| `{id}` | path of `/artefacts/{id}/...` | The short name or identifier of an artefact, i.e. its `short_form` in the `GET /artefacts` response (e.g. `ncit`, `agrovoc`). If several providers host an artefact with the same short name, all of them are queried; use `database` to choose one. |\n" +
                "| `{uri}` | path of `/artefacts/{id}/resources/.../{uri}` | The full IRI of a term, **URL-encoded**, e.g. `http%3A%2F%2Fpurl.obolibrary.org%2Fobo%2FNCIT_C2984`. |\n" +
                "| `uri` | query parameter of `.../children` and `.../tree` | The full IRI of a class or concept, e.g. `uri=http://purl.obolibrary.org/obo/NCIT_C2984`. |\n" +
                "| `{onto}`, `{class}`, `{property}`, `{individual}`, `{entity}` | paths of the OLS endpoints | Same as `{id}` and `{uri}` above, named as in OLS. |\n" +
                "| `iri` | response field | The full IRI of an artefact or term. |\n" +
                "| `short_form` | response field | The short, local identifier of an artefact (e.g. `ncit`) or term (e.g. `NCIT_C2984`). |\n" +
                "| `id` | terminology collections | A UUID generated by the gateway; used as `collectionId`. |\n" +
                "\n" +
                "</details>\n" +
                "<details>\n" +
                "<summary><strong>Global parameters</strong></summary>\n" +
                "\n" +
                "The following query parameters are accepted by all endpoints, except the Authentication, Collections and User endpoints:\n" +
                "\n" +
                "| Parameter | Type | Required | Default | Description |\n" +
                "|-----------|------|----------|---------|-------------|\n" +
                "| `timeout` | integer (milliseconds) | no | the configured maximum (`60000` = 60 seconds, unless configured otherwise) | Maximum time span the gateway waits for each queried provider to respond. Providers that are too slow are skipped. Cannot exceed the configured maximum. See *The timeout parameter* below. |\n" +
                "| `database` | string (comma-separated) | no | all providers | Providers to query, matched against the configured provider names or types (e.g. `ols2`,`ontoportal`, `skosmos`, `tib`, `zbmed`). Unknown values result in an error listing the possible values. |\n" +
                "| `targetDbSchema` | enum: `ontoportal`, `ols2`, `skosmos`, `mod` | no | gateway format | Transforms the response into the schema of the given provider type. |\n" +
                "| `collectionId` | string (UUID) | no | no filter | Restricts the response to artefacts contained in the given terminology collection. The terminology collection must be accessible to the current user. |\n" +
                "| `showResponseConfiguration` | boolean | no | `false` | Additionally returns the original, untransformed provider responses and the request configuration used to query them. |\n" +
                "| `displayEmptyValues` | boolean | no | `true` | If `false`, attributes without a value are removed from the result items. |\n" +
                "| `display` | string (comma-separated) | no | all attributes | Attributes to include in the result items; all other attributes are removed. |\n" +
                "| `omitArtefactsWithoutIri` | boolean | no | `false` | If `true`, only artefacts with a proper IRI as identifier are returned. Only affects artefact (terminology) queries. |\n" +
                "| `disableCache` | boolean | no | `false` | Disables caching of provider responses. Not implemented yet, currently has no effect. |\n" +
                "\n" +
                "Example: `GET /artefacts?database=ols2,ontoportal&targetDbSchema=ols&display=iri,label&timeout=30000`\n" +
                "\n" +
                "</details>\n" +
                "<details>\n" +
                "<summary><strong>The timeout parameter</strong></summary>\n" +
                "\n" +
                "Every request that reads terminology data is forwarded to all queried providers in parallel. " +
                "`timeout` limits how long the gateway waits for each single provider:\n" +
                "\n" +
                "- **A provider answers within the timeout:** its results are included in the aggregated response.\n" +
                "- **A provider does not answer within the timeout:** that provider is skipped and contributes no results. " +
                "The results of all providers that answered in time are returned as usual (partial result). " +
                "The response does not yet indicate which providers were skipped.\n" +
                "- **A provider answers with an error** (e.g. HTTP 404 or 500, or the connection fails): " +
                "that provider is skipped as well, and the results of all other providers are returned as usual.\n" +
                "\n" +
                "This matters most for the search endpoints, which query multiple providers at once. " +
                "Because provider response times vary, **the same request can return results from different providers at different times.** " +
                "A missing or empty result therefore does not necessarily mean that no matching terms exist.\n" +
                "\n" +
                "Further details:\n" +
                "\n" +
                "- The timeout is limited by a maximum that is configured by the gateway administrator and might " +
                "therefore vary between installations of the API Gateway (environment variable `REQUEST_TIMEOUT_MS_MAXVALUE`, default `60000` = 60 seconds). " +
                "If `timeout` is omitted, negative, or larger than this maximum, the maximum is used instead.\n" +
                "- Since the providers are queried in parallel, waiting for them takes at most about `timeout`. " +
                "Aggregating and transforming the results (e.g. for `targetDbSchema`) happens afterwards, " +
                "so the total response time can be slightly longer than `timeout`.\n" +
                "- A smaller `timeout` returns faster but makes it more likely that slow providers are missing from the result. " +
                "To get complete results quickly, use the `database` parameter to query only the providers you need, " +
                "and leave out providers that are known to be slow.\n" +
                "\n" +
                "Example: `GET /search?query=plant&database=ols2,ontoportal&timeout=10000`\n" +
                "\n" +
                "</details>"),
        tags = {
                @Tag(name = "MOD - Catalogue", description = "Information about the gateway itself: the gateway describes itself as one MOD catalogue that combines all connected providers, and links to this documentation."),
                @Tag(name = "MOD - Search", description = "Search for terms (classes, concepts, ...) or for artefacts in all connected providers at once. The best entry point if you do not know yet in which terminology a term is defined."),
                @Tag(name = "MOD - Artefacts", description = "List the artefacts (ontologies, thesauri, vocabularies) of all connected providers, or get the metadata of a single artefact, e.g. title, description, version, and the provider hosting it."),
                @Tag(name = "MOD - Artefact Data", description = "Browse the content of one artefact: its classes/concepts, properties, individuals, SKOS concept schemes, SKOS collections, and the class/concept hierarchy."),
                @Tag(name = "OLS4 v2", description = "Endpoints compatible with the OLS4 v2 API (`/api/v2/...`) for clients that already use OLS. They accept OLS-style paths and parameters, but the data comes from all connected providers, not only from OLS instances, and is returned in OLS4 v2 format. Endpoints marked as not implemented return an error."),
                @Tag(name = "OLS4", description = "Endpoints compatible with the classic OLS API (`/api/...`, as known from OLS3 and still offered by OLS4) for clients that already use OLS. They accept OLS-style paths and parameters, but the data comes from all connected providers and is returned in OLS4 format."),
                @Tag(name = "Collections", description = "Create and manage named sets of terminologies. Pass a collection's ID as collectionId to limit other requests to it. Not to be confused with SKOS collections (see MOD - Artefact Data). Endpoints under `/users/collections` require login."),
                @Tag(name = "Users", description = "Read the profile of the logged-in user, list users, and manage external identifiers such as the ORCID iD. Requires login (see Authentication)."),
                @Tag(name = "Authentication", description = "Register and log in, either with username and password or with single sign-on (SSO) via NFDI AAI. Logging in returns a bearer token (JWT); send it in the `Authorization: Bearer <token>` header (or use the *Authorize* button) to call endpoints that require login. Reading terminology data does not require login."),
                @Tag(name = "Configuration", description = "Show which providers the gateway is connected to, and how the fields of each provider type are mapped to the unified fields of the gateway for artefacts, terms, and search results."),
                @Tag(name = "Status", description = "Check whether the gateway is running, get example requests, and check how the connected providers respond to a given request (response times, status codes, number of results)."),
        }
)
@SecurityScheme(
        name = "BearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class APIGatewayApplication implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(APIGatewayApplication.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${user.admin.password}")
    private String password;

    public APIGatewayApplication(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public static void main(String[] args) {

        SpringApplication.run(APIGatewayApplication.class, args);
    }

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("Async-");
        executor.initialize();
        return executor;
    }

    @Override
    public void run(String... args) throws Exception {
        this.userRepository.findByUsername("admin").ifPresentOrElse(
                user -> logger.info("Admin user already exists"),
                () -> {
                    logger.info("Creating admin user");
                    User admin = new User();
                    admin.setUsername("admin");
                    admin.setPassword(passwordEncoder.encode(password));
                    admin.setRoles(Collections.singleton(Role.ADMIN));
                    this.userRepository.save(admin);
                    logger.info("Admin user created");
                }
        );
    }
}
