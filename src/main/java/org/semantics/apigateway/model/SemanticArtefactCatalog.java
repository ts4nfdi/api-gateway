package org.semantics.apigateway.model;

import lombok.Data;

import java.util.List;

@Data
public class SemanticArtefactCatalog {
    private String acronym;
    private String url;
    private String description;
    private String issued;
    private String title;
}