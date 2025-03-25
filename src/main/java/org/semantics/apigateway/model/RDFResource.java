package org.semantics.apigateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.semantics.apigateway.model.responses.AggregatedResourceBody;

import java.util.List;


@EqualsAndHashCode(callSuper = true)
@Data
public class RDFResource extends AggregatedResourceBody {
    private String iri;
    private String label;
    private List<String> synonyms;
    private List<String> descriptions;

    @JsonProperty("short_form")
    private String shortForm;

    private String ontology;

    @JsonProperty("ontology_iri")
    private String ontologyIri;


    private String source;

    @JsonProperty("source_name")
    private String sourceName;

    @JsonProperty("source_url")
    private String sourceUrl;

    @JsonProperty("backend_type")
    private String backendType;

    private String created;
    private String modified;
    private String version;


    private String type;


    private boolean obsolete;


    public String getTypeURI(){
        return "http://www.w3.org/2000/01/rdf-schema#Resource";
    }
}