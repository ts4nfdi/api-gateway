package org.semantics.apigateway.tracking;

import java.time.Instant;
import java.util.Map;

/**
 * One tracked request, captured by {@link MatomoTrackingInterceptor} and queued for delivery by
 * {@link MatomoTracker}.
 *
 * @param url         the request url with the <em>uri template</em> as its path, never the resolved path, so that
 *                    endpoints carrying an ontology iri in the path do not explode the Matomo action cardinality
 * @param actionName  hierarchical Matomo action name, slash separated, e.g. {@code Search / GET /search}
 * @param visitorId   16 character hexadecimal visitor id
 * @param userId      username of the authenticated user, or null
 * @param searchQuery when set, the event is reported as a Matomo site search instead of a page view
 * @param dimensions  custom dimension index to value
 */
public record MatomoEvent(
        String url,
        String actionName,
        String visitorId,
        String userId,
        String userAgent,
        String language,
        String referrer,
        String clientIp,
        String searchQuery,
        long durationMs,
        Instant timestamp,
        Map<Integer, String> dimensions
) {
}
