package org.semantics.apigateway.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to define the base URI for the JSON-LD context of a property.
 * Example:
 * public class MyClass {
 *  \@ContextUri("http://example.com/name")
 *  private String name;
 *  \@ContextUri("dct/description")
 *  private String description;
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ContextUri {
    String value();
}


