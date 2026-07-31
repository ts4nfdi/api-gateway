package org.semantics.apigateway.tracking;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.semantics.apigateway.service.auth.AuthService;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MatomoTrackingInterceptorTest {

    private static final String CLASSES_TEMPLATE = "/artefacts/{id}/resources/classes/{uri}";

    private MatomoTracker tracker;
    private MatomoProperties properties;
    private AuthService authService;
    private MatomoTrackingInterceptor interceptor;

    @BeforeEach
    void setUp() {
        tracker = mock(MatomoTracker.class);
        authService = mock(AuthService.class);
        properties = new MatomoProperties();
        properties.setVisitorIdSalt("test-salt");
        interceptor = new MatomoTrackingInterceptor(tracker, properties, authService);
    }

    @Test
    @DisplayName("tracks the uri template, never the resolved path carrying the ontology iri")
    void tracksUriTemplateInsteadOfResolvedPath() {
        String iri = "http://purl.obolibrary.org/obo/NCBITaxon_9606";
        MockHttpServletRequest request = request("GET", "/artefacts/agroportal/resources/classes/" + iri, CLASSES_TEMPLATE);

        MatomoEvent event = capture(request, new MockHttpServletResponse(), handler("classes"));

        assertThat(event.url()).endsWith("/api-gateway" + CLASSES_TEMPLATE);
        assertThat(event.url()).doesNotContain(iri);
        assertThat(event.actionName()).isEqualTo("Artefacts / Data / GET " + CLASSES_TEMPLATE);
    }

    @Test
    @DisplayName("derives the category from the springdoc tag of the controller")
    void derivesCategoryFromTag() {
        MockHttpServletRequest request = request("GET", "/search", "/search");

        MatomoEvent event = capture(request, new MockHttpServletResponse(), handler("search"));

        assertThat(event.actionName()).isEqualTo("Search / GET /search");
    }

    @Test
    @DisplayName("falls back to the controller name when no tag is present")
    void fallsBackToControllerName() {
        MockHttpServletRequest request = request("GET", "/untagged", "/untagged");

        MatomoEvent event = capture(request, new MockHttpServletResponse(), untaggedHandler("plain"));

        assertThat(event.actionName()).isEqualTo("Untagged / GET /untagged");
    }

    @Test
    @DisplayName("honours the MatomoTracking name override")
    void honoursNameOverride() {
        MockHttpServletRequest request = request("GET", "/renamed", "/renamed");

        MatomoEvent event = capture(request, new MockHttpServletResponse(), handler("renamed"));

        assertThat(event.actionName()).isEqualTo("Custom / Renamed action");
    }

    @Test
    @DisplayName("skips handlers annotated with NoMatomoTracking")
    void skipsOptedOutHandlers() {
        MockHttpServletRequest request = request("GET", "/secret", "/secret");

        interceptor.preHandle(request, new MockHttpServletResponse(), handler("secret"));
        interceptor.afterCompletion(request, new MockHttpServletResponse(), handler("secret"), null);

        verify(tracker, never()).track(any());
    }

    @Test
    @DisplayName("skips paths matching the configured exclude patterns")
    void skipsExcludedPaths() {
        properties.setExcludePatterns(List.of("/status/**"));
        MockHttpServletRequest request = request("GET", "/status/ping", "/status/ping");

        interceptor.preHandle(request, new MockHttpServletResponse(), handler("search"));
        interceptor.afterCompletion(request, new MockHttpServletResponse(), handler("search"), null);

        verify(tracker, never()).track(any());
    }

    @Test
    @DisplayName("skips non controller handlers such as static resources")
    void skipsNonHandlerMethods() {
        MockHttpServletRequest request = request("GET", "/favicon.ico", "/favicon.ico");

        interceptor.afterCompletion(request, new MockHttpServletResponse(), new Object(), null);

        verify(tracker, never()).track(any());
    }

    @Test
    @DisplayName("records the configured custom dimensions only when an index is set")
    void recordsConfiguredDimensions() {
        properties.getDimensions().setDatabase(1);
        properties.getDimensions().setStatus(3);
        MockHttpServletRequest request = request("GET", "/search", "/search");
        request.setParameter("database", "ols");
        request.setParameter("targetDbSchema", "ols2");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);

        MatomoEvent event = capture(request, response, handler("search"));

        assertThat(event.dimensions()).containsEntry(1, "ols").containsEntry(3, "200");
        // targetDbSchema has no index configured, so it must not be sent.
        assertThat(event.dimensions()).doesNotContainValue("ols2");
    }

    @Test
    @DisplayName("reports the search term as a site search only when enabled")
    void reportsSiteSearchWhenEnabled() {
        MockHttpServletRequest request = request("GET", "/search", "/search");
        request.setParameter("query", "plant");

        assertThat(capture(request, new MockHttpServletResponse(), handler("search")).searchQuery()).isNull();

        properties.setTrackSiteSearch(true);
        assertThat(capture(request, new MockHttpServletResponse(), handler("search")).searchQuery()).isEqualTo("plant");
    }

    @Test
    @DisplayName("produces a 16 character hexadecimal visitor id, stable per user and distinct across users")
    void producesStableSixteenHexVisitorId() {
        String first = interceptor.visitorId("alice", "10.0.0.1", "curl");
        String second = interceptor.visitorId("alice", "10.0.0.9", "other-agent");
        String other = interceptor.visitorId("bob", "10.0.0.1", "curl");

        assertThat(first).hasSize(16).matches("[0-9a-f]{16}");
        assertThat(first).isEqualTo(second).isNotEqualTo(other);
    }

    @Test
    @DisplayName("does not send the username when the user is anonymous")
    void doesNotSendAnonymousPrincipalAsUser() {
        when(authService.getCurrentUsername()).thenReturn("anonymousUser");
        MockHttpServletRequest request = request("GET", "/search", "/search");

        assertThat(capture(request, new MockHttpServletResponse(), handler("search")).userId()).isNull();
    }

    @Test
    @DisplayName("a tracking failure never propagates into the request")
    void swallowsTrackingFailures() {
        when(authService.getCurrentUsername()).thenThrow(new IllegalStateException("boom"));
        MockHttpServletRequest request = request("GET", "/search", "/search");

        interceptor.afterCompletion(request, new MockHttpServletResponse(), handler("search"), null);

        verify(tracker, never()).track(any());
    }

    // -- helpers -----------------------------------------------------------------------------------------------

    private MatomoEvent capture(MockHttpServletRequest request, MockHttpServletResponse response, HandlerMethod handler) {
        MatomoTracker localTracker = mock(MatomoTracker.class);
        MatomoTrackingInterceptor localInterceptor = new MatomoTrackingInterceptor(localTracker, properties, authService);
        localInterceptor.preHandle(request, response, handler);
        localInterceptor.afterCompletion(request, response, handler, null);

        org.mockito.ArgumentCaptor<MatomoEvent> captor = org.mockito.ArgumentCaptor.forClass(MatomoEvent.class);
        verify(localTracker).track(captor.capture());
        return captor.getValue();
    }

    private MockHttpServletRequest request(String method, String path, String template) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, "/api-gateway" + path);
        request.setContextPath("/api-gateway");
        request.setServerName("example.org");
        request.setScheme("https");
        request.setServerPort(443);
        request.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, template);
        return request;
    }

    private HandlerMethod handler(String methodName) {
        return handlerMethod(new TestController(), methodName);
    }

    private HandlerMethod untaggedHandler(String methodName) {
        return handlerMethod(new UntaggedController(), methodName);
    }

    private HandlerMethod handlerMethod(Object bean, String methodName) {
        for (Method method : bean.getClass().getMethods()) {
            if (method.getName().equals(methodName)) {
                return new HandlerMethod(bean, method);
            }
        }
        throw new IllegalArgumentException("No method " + methodName + " on " + bean.getClass());
    }

    @Tag(name = "Artefacts / Data")
    static class TestController {

        @Tag(name = "Search")
        @GetMapping("/search")
        public String search() {
            return "";
        }

        @GetMapping("/artefacts/{id}/resources/classes/{uri}")
        public String classes() {
            return "";
        }

        @MatomoTracking(name = "Renamed action", category = "Custom")
        @GetMapping("/renamed")
        public String renamed() {
            return "";
        }

        @NoMatomoTracking
        @GetMapping("/secret")
        public String secret() {
            return "";
        }
    }

    static class UntaggedController {

        @GetMapping("/untagged")
        public String plain() {
            return "";
        }
    }
}
