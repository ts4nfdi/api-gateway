package org.semantics.apigateway.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "TS4NFDI API Gateway Documentation", version = "1.0"),
        tags = {
                @Tag(
                        name = "MOD - Catalogue",
                        description = "Information about the gateway itself: the gateway describes itself as one " +
                                "MOD catalogue that combines all connected providers."),
                @Tag(
                        name = "MOD - Search",
                        description = "Search for terms (classes, concepts, ...) or for artefacts in all connected " +
                                "providers at once. The best entry point if you do not know yet in which terminology a term is defined."),
                @Tag(
                        name = "MOD - Artefacts",
                        description = "List the artefacts (ontologies, thesauri, vocabularies) of all connected " +
                                "providers, or get the metadata of a single artefact, e.g. title, description, version, and the provider hosting it."),
                @Tag(
                        name = "MOD - Artefact Data",
                        description = "Browse the content of one artefact: its classes/concepts, properties, individuals, " +
                                "SKOS concept schemes, SKOS collections, and the class/concept hierarchy."),
                @Tag(
                        name = "OLS4 v2",
                        description = "Endpoints compatible with the OLS4 v2 API (`/api/v2/...`) for clients that " +
                                "already use OLS. They accept OLS-style paths and parameters, but the data comes from all " +
                                "connected providers, not only from OLS instances, and is returned in OLS4 v2 format. " +
                                "Endpoints marked as not implemented return an error. Both URL prefixes are supported " +
                                "and behave identically: `/ols4/api/` and `/ols/api/`."),
                @Tag(
                        name = "OLS4",
                        description = "Endpoints compatible with the classic OLS API (`/api/...`, as known from OLS3 and " +
                                "still offered by OLS4) for clients that already use OLS. They accept OLS-style paths and " +
                                "parameters, but the data comes from all connected providers and is returned in OLS4 format. " +
                                "Both URL prefixes are supported and behave identically: `/ols4/api/` and `/ols/api/`."),
                @Tag(
                        name = "Collections",
                        description = "Create and manage named sets of terminologies. Pass a collection's ID as collectionId " +
                                "to limit other requests to it. Not to be confused with SKOS collections (see MOD - Artefact Data). " +
                                "Endpoints under `/users/collections` require login."),
                @Tag(
                        name = "Users",
                        description = "Read the profile of the logged-in user, list users, and manage external identifiers " +
                                "such as the ORCID iD. Requires login (see Authentication)."),
                @Tag(
                        name = "Authentication",
                        description = "Register and log in, either with username and password or with single sign-on (SSO) " +
                                "via NFDI AAI. Logging in returns a bearer token (JWT); send it in the `Authorization: " +
                                "Bearer <token>` header (or use the *Authorize* button) to call endpoints that require login. " +
                                "Reading terminology data does not require login."),
                @Tag(
                        name = "Configuration",
                        description = "Show which providers the gateway is connected to, and how the fields of each " +
                                "provider type are mapped to the unified fields of the gateway for artefacts, terms, and search results."),
                @Tag(
                        name = "Status",
                        description = "Check whether the gateway is running, get example requests, and check how the " +
                                "connected providers respond to a given request (response times, status codes, number of results)."),
        }
)
@SecurityScheme(
        name = "BearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {

    private static final String MD_DIR = "classpath:openapi/";

    @Bean
    public OpenApiCustomizer descriptionCustomizer(ResourceLoader loader) {
        String description = String.join("\n",
                markdown(loader, "intro.md"),

                details("Glossary", true,
                        details("MOD, catalogue, semantic artefact", false,
                                markdown(loader, "glossary-mod.md")),
                        details("RDF, OWL, SKOS and the kinds of terms", false,
                                markdown(loader, "glossary-rdf-owl-skos.md")),
                        details("Terminology collection, entity set, provider, and the three kinds of \"collection\"", false,
                                markdown(loader, "glossary-collections.md")),
                        details("Identifiers: IRI, URI, URL, id", false,
                                markdown(loader, "glossary-identifiers.md"))),

                details("Reference", true,
                        details("Connected providers", false,
                                markdown(loader, "providers.md")),
                        details("Global parameters", false,
                                markdown(loader, "global-parameters.md")),
                        details("The timeout parameter", false,
                                markdown(loader, "timeout.md"))));

        return openApi -> {
            if (openApi.getInfo() != null) {
                openApi.getInfo().setDescription(description);
            }
        };
    }

    private static String details(String summary, boolean topLevel, String... parts) {
        String title = topLevel ? "<strong>" + summary + "</strong>" : summary;
        return "<details>\n<summary>" + title + "</summary>\n\n"
                + String.join("\n", parts)
                + "\n</details>\n";
    }

    private static String markdown(ResourceLoader loader, String fileName) {
        try {
            return loader.getResource(MD_DIR + fileName).getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read OpenAPI description file " + fileName, e);
        }
    }
}
