package org.semantics.apigateway.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to define the base URI for the JSON-LD context of a class.
 * Example:
 * \@ContextBaseUri("http://example.com/")
 * public class MyClass {}
 * \@ContextBaseUri("dct") will use the defined namespace dct: http://purl.org/dc/terms/
 * public class MyClass {}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ContextBaseUri {
    String value();
}


