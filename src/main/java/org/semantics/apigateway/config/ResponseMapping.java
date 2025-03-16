package org.semantics.apigateway.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResponseMapping {
    private String nestedJson;
    private String key;

    private String iri;
    private String label;
    private String source;
    private String sourceUrl;
    private String shortForm;
    private String synonyms;
    private String descriptions;
    private String ontology;
    private String type;
    private String obsolete;
    private String OntologyIri;
    private String created;
    private String modified;
    private String version;
    private String status;
    private String versionIRI;
    private String accessRights;
    private String license;
    private String contactPoint;
    private String creator;
    private String identifier;
    private String keywords;
    private String landingPage;
    private String language;
    private String publisher;
    private String subject;
    private String accrualMethod;
    private String accrualPeriodicity;
    private String bibliographicCitation;
    private String contributor;
    private String coverage;
    private String hasFormat;
    private String rightsHolder;
    private String competencyQuestion;
    private String semanticArtefactRelation;
    private String createdWith;
    private String wasGeneratedBy;
    private String includedInDataCatalog;

    public Map<String, String> inverseMapping() {
        return Arrays.stream(getClass().getDeclaredFields())
                .map(field -> {
                    try {
                        field.setAccessible(true);
                        Object value = field.get(this);
                        return value != null
                                ? Map.entry(value.toString(), field.getName())
                                : null;
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (v1, v2) -> v1  // In case of duplicate keys, keep the first value
                ));
    }

    public Map<String, String> toMap(){
        Map<String, String> mappingValues = new HashMap<>();

        // Use reflection to get all getter methods from ResponseMapping
        Method[] methods = ResponseMapping.class.getDeclaredMethods();
        for (Method method : methods) {
            String methodName = method.getName();
            if (methodName.startsWith("get") && !methodName.equals("getClass")) {
                try {
                    Object result = method.invoke(this);
                    if (result instanceof String) {
                        String fieldName = methodName.substring(3); // Remove "get"
                        fieldName = fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1); // Convert to camelCase
                        mappingValues.put(fieldName, (String) result);
                    }
                } catch (Exception e) {
                    // Skip if unable to invoke method
                }
            }
        }
        return mappingValues;
    }
}