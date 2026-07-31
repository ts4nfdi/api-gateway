package org.semantics.apigateway.tracking;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for the server side Matomo tracking of the gateway endpoints.
 * Everything is disabled by default, so the tracking stays inert until a deployment opts in.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "matomo")
public class MatomoProperties {

    /**
     * Master switch. When false no interceptor is registered and no tracker bean is created.
     */
    private boolean enabled = false;

    /**
     * Full URL of the Matomo tracking endpoint, e.g. https://matomo.example.org/matomo.php
     */
    private String url = "";

    /**
     * The Matomo site id the gateway traffic belongs to.
     */
    private int siteId = 1;

    /**
     * Only required for parameters Matomo gates behind authentication (currently the visitor ip override).
     */
    private String tokenAuth = "";

    /**
     * Salt for the visitor id hash. A random one is generated at startup when left empty, which means
     * visitor ids are not comparable across restarts. Set it explicitly to get stable ids.
     */
    private String visitorIdSalt = "";

    /**
     * Send the real client ip to Matomo. Requires tokenAuth. Without it Matomo attributes every visit to the
     * gateway host itself, so there is no country/city reporting.
     */
    private boolean trackClientIp = false;

    /**
     * Anonymise the client ip by zeroing its last octet (IPv4) or its last 80 bits (IPv6) before sending.
     * Only relevant when trackClientIp is true.
     */
    private boolean anonymizeClientIp = true;

    /**
     * Report the username of authenticated users to Matomo as the user id.
     */
    private boolean trackUserId = true;

    /**
     * Report the "query" request parameter of the search endpoints as a Matomo site search.
     * Off by default because it records user supplied content.
     */
    private boolean trackSiteSearch = false;

    /**
     * Ant style patterns, matched against the request path without the context path, that must not be tracked.
     */
    private List<String> excludePatterns = new ArrayList<>(List.of(
            "/actuator/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/doc/api",
            "/status/**",
            "/auth/**",
            "/error"
    ));

    /**
     * Maximum number of events shipped in a single bulk request.
     */
    private int batchSize = 50;

    /**
     * Delay between two flushes of the event queue, in milliseconds.
     */
    private long flushIntervalMs = 10_000;

    /**
     * Upper bound of the in memory event queue. Events are dropped once it is full so that tracking can never
     * slow down or break an API request.
     */
    private int queueCapacity = 10_000;

    /**
     * Timeout of a single bulk request towards Matomo, in milliseconds.
     */
    private long timeoutMs = 5_000;

    /**
     * Maps a logical dimension to the custom dimension index configured in the Matomo UI.
     * A missing or non positive index means the dimension is not sent.
     */
    private Dimensions dimensions = new Dimensions();

    @Getter
    @Setter
    public static class Dimensions {
        /** Index of the custom dimension receiving the "database" request parameter. */
        private int database = 0;
        /** Index of the custom dimension receiving the "targetDbSchema" request parameter. */
        private int targetDbSchema = 0;
        /** Index of the custom dimension receiving the HTTP response status. */
        private int status = 0;
        /** Index of the custom dimension receiving the authentication kind (anonymous / authenticated). */
        private int auth = 0;
    }
}
