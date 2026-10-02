package org.semantics.apigateway.tracking;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Overrides the Matomo action name that {@link MatomoTrackingInterceptor} derives from the springdoc
 * {@code @Tag} and the uri template. Optional: only use it where the derived name reads badly.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface MatomoTracking {

    /**
     * Replaces the {@code METHOD /uri/template} part of the action name.
     */
    String name() default "";

    /**
     * Replaces the category, which defaults to the springdoc tag of the controller.
     * Slashes create additional levels in the Matomo page hierarchy.
     */
    String category() default "";
}
