package org.semantics.apigateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.semantics.apigateway.model.responses.AggregatedResourceBody;


@EqualsAndHashCode(callSuper = true)
@Data
@ContextBaseUri("base4nfdi")
public class RDFResource extends AggregatedResourceBody {
    private String ontology;

    @JsonProperty("ontology_iri")
    private String ontologyIri;

    public String getTypeURI(){
        return "http://www.w3.org/2000/01/rdf-schema#Resource";
    }


    public String getOntology() {
        if (ontology == null) {
            ontology = ontologyIri;
        }
        return ontology.substring(ontology.lastIndexOf('/') + 1);
    }
}