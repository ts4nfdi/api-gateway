package org.semantics.apigateway.tracking;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.semantics.apigateway.service.auth.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;

/**
 * Tracks every request handled by a controller, without touching the controllers themselves.
 *
 * <p>The action reported to Matomo is built from the <em>uri template</em> and the springdoc {@code @Tag} of the
 * controller. Using the template rather than the resolved path is what keeps the number of distinct Matomo actions
 * bounded: endpoints such as {@code /artefacts/{id}/resources/classes/{uri}} carry a full ontology iri in their path.
 * This is also why the tracking is an interceptor and not a servlet filter, as a filter runs before handler mapping
 * and therefore cannot see the template.
 */
public class MatomoTrackingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(MatomoTrackingInterceptor.class);

    private static final String START_TIME_ATTRIBUTE = MatomoTrackingInterceptor.class.getName() + ".start";
    private static final String CONTROLLER_SUFFIX = "Controller";
    private static final String ANONYMOUS_USER = "anonymousUser";
    private static final int VISITOR_ID_LENGTH = 16;

    private final MatomoTracker tracker;
    private final MatomoProperties properties;
    private final AuthService authService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final String visitorIdSalt;

    public MatomoTrackingInterceptor(MatomoTracker tracker, MatomoProperties properties, AuthService authService) {
        this.tracker = tracker;
        this.properties = properties;
        this.authService = authService;
        this.visitorIdSalt = resolveSalt(properties.getVisitorIdSalt());
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (request.getAttribute(START_TIME_ATTRIBUTE) == null) {
            request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());
        }
        return true;
    }

    /**
     * Runs after the response is complete so the duration and the outcome are known. Never throws: a tracking
     * problem must not turn a successful response into a failure.
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        try {
            if (!(handler instanceof HandlerMethod handlerMethod)) {
                return;
            }
            if (isExcluded(request, handlerMethod)) {
                return;
            }
            tracker.track(buildEvent(request, response, handlerMethod));
        } catch (Exception e) {
            logger.debug("Skipped Matomo tracking for {}: {}", request.getRequestURI(), e.toString());
        }
    }

    private MatomoEvent buildEvent(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod) {
        String template = uriTemplate(request);
        String clientIp = clientIp(request);
        String username = currentUsername();

        return new MatomoEvent(
                buildUrl(request, template),
                buildActionName(handlerMethod, request.getMethod(), template),
                visitorId(username, clientIp, request.getHeader("User-Agent")),
                username,
                request.getHeader("User-Agent"),
                request.getHeader("Accept-Language"),
                request.getHeader("Referer"),
                properties.isTrackClientIp() ? maybeAnonymize(clientIp) : null,
                searchQuery(request, template),
                duration(request),
                Instant.now(),
                dimensions(request, response, username)
        );
    }

    // -- action naming -------------------------------------------------------------------------------------------

    /**
     * {@code <category> / <METHOD> <uri template>}, e.g. {@code Artefacts / Data / GET /artefacts/{id}}.
     * Matomo splits the action name on slashes to build its page hierarchy, so this mirrors the swagger grouping
     * that the controllers already declare.
     */
    String buildActionName(HandlerMethod handlerMethod, String httpMethod, String template) {
        MatomoTracking override = findAnnotation(handlerMethod, MatomoTracking.class);

        String category = override != null && !override.category().isBlank()
                ? override.category()
                : resolveCategory(handlerMethod);

        String action = override != null && !override.name().isBlank()
                ? override.name()
                : httpMethod + " " + template;

        return category.isBlank() ? action : category + " / " + action;
    }

    private String resolveCategory(HandlerMethod handlerMethod) {
        Tag tag = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(), Tag.class);
        if (tag == null) {
            tag = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), Tag.class);
        }
        if (tag != null && !tag.name().isBlank()) {
            return tag.name();
        }
        String type = handlerMethod.getBeanType().getSimpleName();
        return type.endsWith(CONTROLLER_SUFFIX)
                ? type.substring(0, type.length() - CONTROLLER_SUFFIX.length())
                : type;
    }

    // -- request inspection --------------------------------------------------------------------------------------

    /**
     * The matched uri template, e.g. {@code /artefacts/{id}/resources/classes/{uri}}. Falls back to the path within
     * the application when no pattern is exposed, which should not happen for a {@link HandlerMethod}.
     */
    private String uriTemplate(HttpServletRequest request) {
        Object pattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        return pattern != null ? pattern.toString() : pathWithinApplication(request);
    }

    private String buildUrl(HttpServletRequest request, String template) {
        StringBuffer requestUrl = request.getRequestURL();
        String path = request.getRequestURI();
        int pathStart = requestUrl.length() - path.length();
        String origin = pathStart > 0 ? requestUrl.substring(0, pathStart) : "";
        return origin + request.getContextPath() + template;
    }

    private String pathWithinApplication(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            uri = uri.substring(contextPath.length());
        }
        return uri.isEmpty() ? "/" : uri;
    }

    private boolean isExcluded(HttpServletRequest request, HandlerMethod handlerMethod) {
        if (findAnnotation(handlerMethod, NoMatomoTracking.class) != null) {
            return true;
        }
        String path = pathWithinApplication(request);
        String template = uriTemplate(request);
        for (String pattern : properties.getExcludePatterns()) {
            if (pathMatcher.match(pattern, path) || pathMatcher.match(pattern, template)) {
                return true;
            }
        }
        return false;
    }

    private long duration(HttpServletRequest request) {
        Object start = request.getAttribute(START_TIME_ATTRIBUTE);
        return start instanceof Long startedAt ? System.currentTimeMillis() - startedAt : 0L;
    }

    /**
     * Reports the search term of the search endpoints as a Matomo site search, which gives a dedicated report of what
     * users look up in the terminologies. Returns null unless the endpoint is one of the search endpoints.
     */
    private String searchQuery(HttpServletRequest request, String template) {
        if (!properties.isTrackSiteSearch()) {
            return null;
        }
        if (!template.contains("/search") && !template.contains("/select")) {
            return null;
        }
        String query = request.getParameter("query");
        return query != null && !query.isBlank() ? query : null;
    }

    private Map<Integer, String> dimensions(HttpServletRequest request, HttpServletResponse response, String username) {
        Map<Integer, String> dimensions = new HashMap<>();
        MatomoProperties.Dimensions indices = properties.getDimensions();

        putDimension(dimensions, indices.getDatabase(), request.getParameter("database"));
        putDimension(dimensions, indices.getTargetDbSchema(), request.getParameter("targetDbSchema"));
        putDimension(dimensions, indices.getStatus(), String.valueOf(response.getStatus()));
        putDimension(dimensions, indices.getAuth(), username != null ? "authenticated" : "anonymous");

        return dimensions;
    }

    private void putDimension(Map<Integer, String> dimensions, int index, String value) {
        if (index > 0 && value != null && !value.isBlank()) {
            dimensions.put(index, value);
        }
    }

    private String currentUsername() {
        // Deliberately not AuthService#tryGetCurrentUser, which loads the user from the database: this runs on every
        // request and only the username is needed.
        String username = authService.getCurrentUsername();
        return username == null || ANONYMOUS_USER.equals(username) ? null : username;
    }

    private <A extends java.lang.annotation.Annotation> A findAnnotation(HandlerMethod handlerMethod, Class<A> type) {
        A annotation = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(), type);
        return annotation != null ? annotation : AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), type);
    }

    // -- visitor identification ----------------------------------------------------------------------------------

    /**
     * Matomo expects a 16 character hexadecimal visitor id. Authenticated users get a stable pseudonym derived from
     * their username. Anonymous visitors get one derived from ip, user agent and the current date, so that it rotates
     * daily instead of acting as a persistent identifier.
     */
    String visitorId(String username, String clientIp, String userAgent) {
        String seed = username != null
                ? "user:" + username
                : "anon:" + clientIp + "|" + userAgent + "|" + LocalDate.now(ZoneOffset.UTC);
        return hash(visitorIdSalt + seed);
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes).substring(0, VISITOR_ID_LENGTH);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required to build a Matomo visitor id", e);
        }
    }

    private static String resolveSalt(String configured) {
        if (configured != null && !configured.isBlank()) {
            return configured;
        }
        byte[] random = new byte[32];
        new SecureRandom().nextBytes(random);
        logger.info("No matomo.visitor-id-salt configured, generated a random one. "
                + "Visitor ids will not be stable across restarts.");
        return HexFormat.of().formatHex(random);
    }

    // -- client ip -----------------------------------------------------------------------------------------------

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            // The first hop is the original client, the rest are the proxies it went through.
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Zeroes the last octet of an IPv4 address, or everything after the network prefix of an IPv6 address.
     */
    private String maybeAnonymize(String ip) {
        if (ip == null || ip.isBlank() || !properties.isAnonymizeClientIp()) {
            return ip;
        }
        if (ip.contains(":")) {
            String[] groups = ip.split(":");
            StringBuilder anonymized = new StringBuilder();
            for (int i = 0; i < 3 && i < groups.length; i++) {
                anonymized.append(groups[i]).append(':');
            }
            return anonymized.append(':').toString();
        }
        int lastDot = ip.lastIndexOf('.');
        return lastDot > 0 ? ip.substring(0, lastDot) + ".0" : ip;
    }
}
