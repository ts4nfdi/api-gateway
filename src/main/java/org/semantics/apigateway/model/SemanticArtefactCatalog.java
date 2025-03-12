package org.semantics.apigateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class SemanticArtefactCatalog {
    @JsonProperty("@id")
    private String id;
    @JsonProperty("@type")
    private String type;

    private String title;
    private String description;
    private String status;
    private String language;
    private String accessRights;
    private String license;
    private String rightsHolder;
    private String landingPage;
    private String accessURL;
    private String identifier;
    private String keyword;
    private String bibliographicCitation;
    private String created;
    private String modified;
    private String createdWith;
    private String creator;
    private String contributor;
    private String publisher;
    private String contactPoint;
    private String subject;
    private String coverage;
    private String accrualMethod;
    private String accrualPeriodicity;
    private String wasGeneratedBy;

    private Map<String, String> links;

    @JsonProperty("@context")
    private Map<String, String> context;
}