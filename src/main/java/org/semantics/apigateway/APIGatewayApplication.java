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
        info = @Info(title = "API Gateway Documentation", version = "1.0", description = "The TS4NFDI Federated Service is an advanced, dynamic solution designed to perform federated calls across multiple Terminology Services (TS) within NFDI. It is particularly tailored for environments where integration and aggregation of diverse data sources are essential. The service offers search capabilities, enabling users to refine search results based on specific criteria, and supports responses in both JSON and JSON-LD formats.\n" +
                "\n" +
                "A standout feature of this service is its dynamic nature, governed by a JSON configuration file. This design choice allows for easy extension and customization of the service to include new TS or modify existing configurations.\n" +
                "\n" +
                "<details>\n" +
                "<summary><strong>Global parameters</strong></summary>\n" +
                "\n" +
                "The following query parameters are accepted by all endpoints, except the Authentication, Collections and User endpoints:\n" +
                "\n" +
                "| Parameter | Type | Required | Default | Description |\n" +
                "|-----------|------|----------|---------|-------------|\n" +
                "| `timeout` | integer (milliseconds) | no | `60000` (60 seconds) | Maximum time span the gateway waits for all queried providers to respond. If one provider is too slow, the result is empty. See *The timeout parameter* below. |\n" +
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
                "</details>" +
                "<details>\n" +
                "<summary><strong>The timeout parameter</strong></summary>\n" +
                "\n" +
                "Every request that reads terminology data is forwarded to all queried providers in parallel. " +
                "The gateway waits until every provider has answered and only then aggregates the results into one response. " +
                "`timeout` limits how long the gateway waits:\n" +
                "\n" +
                "- **All providers answer within the timeout:** the response contains the aggregated results of all providers.\n" +
                "- **At least one provider does not answer within the timeout:** the response contains **no results at all**. " +
                "Results of providers that answered in time are discarded as well; there are no partial results. " +
                "The request still succeeds, but with an empty result list.\n" +
                "- **A provider answers with an error** (e.g. HTTP 404 or 500, or the connection fails) within the timeout: " +
                "that provider contributes no results, and the results of all other providers are returned as usual.\n" +
                "\n" +
                "This matters most for the search endpoints, which query multiple providers at once. " +
                "Because provider response times vary, **the same request can return a full result one time and an empty result another time.** " +
                "An empty search result therefore does not necessarily mean that no matching terms exist.\n" +
                "\n" +
                "Further details:\n" +
                "\n" +
                "- The timeout only covers waiting for the providers. Aggregating and transforming the results (e.g. for `targetDbSchema`) happens afterwards, " +
                "so the total response time can be slightly longer than `timeout`.\n" +
                "- Independently of `timeout`, each single provider request is aborted after about 60 seconds without a connection or without data (connect and read timeouts). " +
                "Such a provider then counts as failed and is skipped. " +
                "This means a `timeout` above 60000 lets the gateway return the results of the remaining providers instead of an empty result, " +
                "but it does not give a slow provider more than about 60 seconds to answer.\n" +
                "- A smaller `timeout` returns faster but makes empty results more likely. " +
                "To get reliable results quickly, use the `database` parameter to query only the providers you need, " +
                "and leave out providers that are known to be slow.\n" +
                "\n" +
                "Example: `GET /search?query=plant&database=ols2,ontoportal&timeout=10000`\n" +
                "\n" +
                "</details>"),
        tags = {
                @Tag(name = "MOD - Search", description = "Search across all connected providers."),
                @Tag(name = "MOD - Artefacts", description = "List the artefacts of all connected providers and get the metadata of a single artefact."),
                @Tag(name = "MOD - Artefact Data", description = "Browse the content of an artefact: classes/concepts, properties, individuals, concept schemes, skos:collections, and the class/concept hierarchy."),
                @Tag(name = "OLS4 v2", description = "OLS4 v2 compatible endpoints for clients that already use the OLS4 API. Responses come from all connected providers in OLS4 v2 format."),
                @Tag(name = "OLS4", description = "OLS4 (OLS3-style) compatible endpoints for clients that already use the OLS4 API. Responses come from all connected providers in OLS4 format."),
                @Tag(name = "Collections", description = "Create and manage named sets of terminologies. Pass a collection's ID as collectionId to limit other requests to it."),
                @Tag(name = "Users", description = "Read and update user profiles, including ORCID and other external identifiers."),
                @Tag(name = "Authentication", description = "Register, log in and out, and single sign-on. Returns the bearer token (JWT) needed by protected endpoints."),
                @Tag(name = "Configuration", description = "Show the connected providers and the metadata fields the gateway maps for artefacts, terms and search."),
                @Tag(name = "Status", description = "Check the health and availability of the gateway and the connected providers."),
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
