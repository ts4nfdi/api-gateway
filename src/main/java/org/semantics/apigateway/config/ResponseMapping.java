package org.semantics.apigateway.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResponseMapping {
    private String nestedJson;
    private String iri;
    private String label;
    private String source;
    private String sourceUrl;
    private String backendType;
    private String shortForm;
    private String synonym;
    private String ontology;
    private String key;
    private String description;
    private String type;
}