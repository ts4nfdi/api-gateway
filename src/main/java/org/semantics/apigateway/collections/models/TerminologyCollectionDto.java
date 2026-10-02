package org.semantics.apigateway.collections.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import ioinformarics.oss.jackson.module.jsonld.annotation.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.jena.iri.IRIFactory;

import java.util.Collections;
import java.util.List;

@NoArgsConstructor
@Data
@JsonldResource
@JsonldNamespace(name="collection", uri="https://w3id.org/ts4nfdi/collection/", applyToProperties = false)
@JsonldType("collection:Collection")
public class TerminologyCollectionDto {
    
    private static final IRIFactory iriFactory = IRIFactory.iriImplementation();
    
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonldId
    @JsonldProperty("http://base4nfdi.de/ts4nfdi/schema/iri")
    String id;
    
    @NotNull
    @JsonldProperty("http://www.w3.org/2000/01/rdf-schema#label")
    String label;
    
    @NotNull
    @JsonldProperty("http://purl.org/dc/terms/creator")
    String creator;
    
    @NotNull
    @JsonldProperty("http://purl.org/dc/terms/description")
    String description;
    
    @NotNull
    @JsonProperty("collection:isPublic")
    boolean isPublic;
    
    @JsonldProperty("collection:collaborators")
    List<CollectionCollaboratorDto> collaborators = Collections.emptyList();
    
    
    @JsonldProperty("collection:terminologies")
    List<ResourceDto> terminologies = Collections.emptyList();


    public static TerminologyCollectionDto toDto(TerminologyCollection x) {
        TerminologyCollectionDto dto = new TerminologyCollectionDto();
        dto.setId(iriFactory.create("https://w3id.org/ts4nfdi/collection/" + x.getId()).toString());
        dto.setLabel(x.getLabel());
        dto.setDescription(x.getDescription());
        dto.setPublic(x.isPublic());
        dto.setCreator(x.getUser().getUsername());
        dto.setCollaborators(x.getCollaborators().stream().map(c -> new CollectionCollaboratorDto(
                c.getUser().getUsername(),
                c.getRole()
        )).toList());
        dto.setTerminologies(x.getTerminologies().stream().map(r -> new ResourceDto(
                r.getUri(),
                r.getLabel(),
                r.getSource(),
                r.getType()
        )).toList());
        return dto;
    }
}

