package org.semantics.apigateway.tracking;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Excludes a handler method, or every handler method of a controller, from Matomo tracking.
 * Endpoints are tracked by default, so this is only needed for the exceptions.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface NoMatomoTracking {
}
