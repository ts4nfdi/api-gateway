package org.semantics.apigateway.model;

import lombok.Data;

import java.util.List;


@Data
public class RDFResource {
    private List<String> labels;
    private List<String> synonyms;
    private String iri;
    private String description;
    private String type;
    private SemanticArtefact semanticArtefact;
    private SemanticArtefactCatalog semanticArtefactCatalog;
}