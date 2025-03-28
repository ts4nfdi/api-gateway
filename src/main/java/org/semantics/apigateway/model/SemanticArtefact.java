package org.semantics.apigateway.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.semantics.apigateway.model.responses.AggregatedResourceBody;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@ContextBaseUri("dct")
public class SemanticArtefact extends AggregatedResourceBody {

    private String versionIRI;
    private String accessRights;
    private String license;
    private String identifier;
    private List<String> language;
    private List<String> subject;
    private String accrualMethod;
    private String accrualPeriodicity;
    private List<String> bibliographicCitation;
    private List<String> creator;
    private List<String> contributor;
    private List<String> rightsHolder;
    private List<String> publisher;
    private String coverage;
    private List<String> hasFormat;


    @ContextUri("dcat")
    private List<String> keywords;
    @ContextUri("dcat")
    private String landingPage;
    @ContextUri("dcat")
    private List<String> contactPoint;

    @ContextUri("mod")
    private String competencyQuestion;
    @ContextUri("mod")
    private String semanticArtefactRelation;
    @ContextUri("mod")
    private String status;
    @ContextUri("mod")
    private List<String> wasGeneratedBy;

    @ContextUri("pav")
    private List<String> createdWith;


    //TODO use this instead of source in the future
    @ContextUri("schema")
    private List<String> includedInDataCatalog;

    @Override
    public String getTypeURI() {
        return "https://w3id.org/mod#SemanticArtefact";
    }
}