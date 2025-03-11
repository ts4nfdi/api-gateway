package org.semantics.apigateway.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;
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
}