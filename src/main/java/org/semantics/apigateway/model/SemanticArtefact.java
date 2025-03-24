package org.semantics.apigateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.semantics.apigateway.model.responses.AggregatedResourceBody;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class SemanticArtefact extends AggregatedResourceBody {
    private String label;
    private List<String> synonyms;
    private List<String> descriptions;
    private String ontology;
    @JsonProperty("ontology_iri")
    private String ontologyIri;
    private String type;


    private String created;
    private String modified;
    private String version;


    private boolean obsolete;
    private String status;
    private String versionIRI;
    private String accessRights;
    private String license;
    private String identifier;
    private List<String> keywords;
    private String landingPage;
    private List<String> language;
    private List<String> subject;
    private String accrualMethod;
    private String accrualPeriodicity;

    private List<String> bibliographicCitation;

    private List<String> contactPoint;
    private List<String> creator;
    private List<String> contributor;
    private List<String> rightsHolder;
    private List<String> publisher;

    private String coverage;
    private String hasFormat;
    private String competencyQuestion;
    private String semanticArtefactRelation;
    private List<String> createdWith;
    private List<String> wasGeneratedBy;

    //TODO use this instead of source in the future
    private List<String> includedInDataCatalog;
}