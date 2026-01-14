package org.semantics.apigateway.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.semantics.apigateway.model.responses.AggregatedResourceBody;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@EqualsAndHashCode(callSuper = true)
@Data
@ContextBaseUri("base4nfdi")
public class RDFResource extends AggregatedResourceBody {
    private String ontology;

    @JsonProperty("ontology_iri")
    private String ontologyIri;
    
    @JsonProperty("depiction")
    @ContextUri("foaf:depiction")
    protected String depiction;

    private boolean hasChildren = false;

    private List<RDFResource> children = new ArrayList<>();

    public String getTypeURI() {
        return "http://www.w3.org/2000/01/rdf-schema#Resource";
    }


    public String getOntologyIri() {
        if (ontologyIri == null || ontologyIri.isEmpty()) {
            ontologyIri = source;
        }

        return ontologyIri;
    }

    public String getOntology() {
        // TODO: how to not hardcode this case of nerc vocabulary?
        if (ontology != null && ontology.contains("vocab.nerc.ac.uk")) {
            Matcher matcher = Pattern.compile("/collection/([^/]+)/").matcher(ontology);
            if (matcher.find()) {
                ontology = matcher.group(1);
            }
        }

        if ((ontology == null || ontology.isEmpty()) && ontologyIri != null && !ontologyIri.isEmpty()) {
            ontology = ontologyIri.substring(ontologyIri.lastIndexOf('/') + 1);
        }

        if (ontology == null || ontology.isEmpty()) {
            ontologyIri = source;
            ontology = sourceName;
        }

        return ontology.substring(ontology.lastIndexOf('/') + 1);
    }
}