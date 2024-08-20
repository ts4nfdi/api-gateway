package org.semantics.apigateway.model;

import lombok.Data;
import java.util.List;

@Data
public class SemanticArtefact {
    private String acronym;
    private String URI;
    private String description;
    private String issued;
    private String title;
    private List<String> sources;
}