package org.semantics.apigateway.collections.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@Data
public class TerminologyCollectionDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    UUID id;
    @NotNull
    String label;
    @NotNull
    String creator;
    @NotNull
    String description;
    @NotNull
    @JsonProperty("isPublic")
    boolean isPublic;
    List<CollectionCollaboratorDto> collaborators = Collections.emptyList();
    List<ResourceDto> terminologies = Collections.emptyList();


    public static TerminologyCollectionDto toDto(TerminologyCollection x) {
        TerminologyCollectionDto dto = new TerminologyCollectionDto();
        dto.setId(x.getId());
        dto.setLabel(x.getLabel());
        dto.setDescription(x.getDescription());
        dto.setPublic(x.isPublic());
        dto.setCreator(x.getUser().getUsername());
        dto.setCollaborators(x.getCollaborators().stream().map(c -> {
            return new CollectionCollaboratorDto(
                    c.getUser().getUsername(),
                    c.getRole()
            );
        }).toList());
        dto.setTerminologies(x.getTerminologies().stream().map(r -> {
            return new ResourceDto(
                    r.getUri(),
                    r.getLabel(),
                    r.getSource(),
                    r.getType()
            );
        }).toList());
        return dto;
    }
}

