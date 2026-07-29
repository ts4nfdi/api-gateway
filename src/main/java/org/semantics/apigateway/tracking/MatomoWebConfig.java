package org.semantics.apigateway.tracking;

import org.semantics.apigateway.service.auth.AuthService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registers the tracking interceptor for every endpoint. Nothing here is created unless {@code matomo.enabled=true},
 * so the feature is inert until a deployment opts in.
 *
 * <p>Scheduling is enabled here rather than on the application class so that turning the tracking off also removes
 * the flush scheduler.
 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties(MatomoProperties.class)
@ConditionalOnProperty(prefix = "matomo", name = "enabled", havingValue = "true")
public class MatomoWebConfig implements WebMvcConfigurer {

    private final MatomoTracker tracker;
    private final MatomoProperties properties;
    private final AuthService authService;

    public MatomoWebConfig(MatomoTracker tracker, MatomoProperties properties, AuthService authService) {
        this.tracker = tracker;
        this.properties = properties;
        this.authService = authService;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // No path patterns here: every controller endpoint is tracked, and the exclusions are applied inside the
        // interceptor so that they stay configurable at runtime through matomo.exclude-patterns.
        registry.addInterceptor(new MatomoTrackingInterceptor(tracker, properties, authService));
    }
}
