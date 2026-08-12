package org.semantics.apigateway.tracking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MatomoTrackerTest {

    private MatomoProperties properties;
    private MatomoTracker tracker;

    @BeforeEach
    void setUp() {
        properties = new MatomoProperties();
        properties.setSiteId(36);
        properties.setUrl("https://example.de/matomo.php");
        properties.setQueueCapacity(2);
        tracker = new MatomoTracker(properties, WebClient.builder());
    }

    @Test
    @DisplayName("builds a bulk entry with the mandatory Matomo parameters")
    void buildsQueryWithMandatoryParameters() {
        String query = tracker.buildQuery(event("Search / GET /search", null));

        assertThat(query).startsWith("?idsite=36&rec=1&send_image=0&cdt=");
        assertThat(query).contains("&url=https%3A%2F%2Fterminology.services.base4nfdi.de%2Fapi-gateway%2Fsearch");
        assertThat(query).contains("&action_name=Search+%2F+GET+%2Fsearch");
        assertThat(query).contains("&_id=0123456789abcdef");
    }

    @Test
    @DisplayName("sends the real event time so queueing delay does not skew the reports")
    void sendsEventTimestamp() {
        Instant when = Instant.parse("2026-07-27T10:15:30Z");
        MatomoEvent event = new MatomoEvent("https://terminology.services.base4nfdi.de/api-gateway/search", "Search / GET /search",
                "0123456789abcdef", null, null, null, null, null, null, 0, when, Map.of());

        assertThat(tracker.buildQuery(event)).contains("&cdt=" + when.getEpochSecond());
    }

    @Test
    @DisplayName("only writes custom dimensions with a positive index")
    void writesOnlyConfiguredDimensions() {
        String query = tracker.buildQuery(event("Search / GET /search", Map.of(1, "ols", 0, "ignored")));

        assertThat(query).contains("&dimension1=ols");
        assertThat(query).doesNotContain("dimension0");
    }

    @Test
    @DisplayName("omits the search parameter unless site search tracking is enabled")
    void omitsSearchUnlessEnabled() {
        MatomoEvent event = new MatomoEvent("https://terminology.services.base4nfdi.de/api-gateway/search", "Search / GET /search",
                "0123456789abcdef", null, null, null, null, null, "plant", 0, Instant.now(), Map.of());

        assertThat(tracker.buildQuery(event)).doesNotContain("&search=");

        properties.setTrackSiteSearch(true);
        assertThat(tracker.buildQuery(event)).contains("&search=plant");
    }

    @Test
    @DisplayName("omits the client ip unless ip tracking is enabled")
    void omitsClientIpUnlessEnabled() {
        MatomoEvent event = new MatomoEvent("https://terminology.services.base4nfdi.de/api-gateway/search", "Search / GET /search",
                "0123456789abcdef", null, null, null, null, "203.0.113.0", null, 0, Instant.now(), Map.of());

        assertThat(tracker.buildQuery(event)).doesNotContain("&cip=");

        properties.setTrackClientIp(true);
        assertThat(tracker.buildQuery(event)).contains("&cip=203.0.113.0");
    }

    @Test
    @DisplayName("drops events instead of blocking once the queue is full")
    void dropsEventsWhenQueueIsFull() {
        assertThat(tracker.track(event("a", null))).isTrue();
        assertThat(tracker.track(event("b", null))).isTrue();

        assertThat(tracker.track(event("c", null))).isFalse();
        assertThat(tracker.getDroppedCount()).isEqualTo(1);
        assertThat(tracker.getQueueSize()).isEqualTo(2);
    }

    @Test
    @DisplayName("a null event is ignored rather than queued")
    void ignoresNullEvents() {
        assertThat(tracker.track(null)).isFalse();
        assertThat(tracker.getQueueSize()).isZero();
    }

    private MatomoEvent event(String actionName, Map<Integer, String> dimensions) {
        return new MatomoEvent(
                "https://terminology.services.base4nfdi.de/api-gateway/search",
                actionName,
                "0123456789abcdef",
                null, null, null, null, null, null,
                0,
                Instant.now(),
                dimensions == null ? Map.of() : dimensions
        );
    }
}
