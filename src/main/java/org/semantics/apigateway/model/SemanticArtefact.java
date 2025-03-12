package org.semantics.apigateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SemanticArtefact {
    @JsonProperty("@id")
    private String id;
    @JsonProperty("@type")
    private String type;

    private String acronym;
    private String description;
    private String versionIRI;
    private String accessRights;
    private String license;
    private String contactPoint;
    private String creator;
    private String identifier;
    private String keyword;
    private String landingPage;
    private String language;
    private String publisher;
    private String subject;
    private String title;
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
}