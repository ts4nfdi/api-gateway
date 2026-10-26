package org.semantics.apigateway.collections.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Comparator;
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
    List<CollectionStatisticDto> statistics = Collections.emptyList();


    public static TerminologyCollectionDto toDto(TerminologyCollection x) {
        TerminologyCollectionDto dto = new TerminologyCollectionDto();
        dto.setId(x.getId());
        dto.setLabel(x.getLabel());
        dto.setDescription(x.getDescription());
        dto.setPublic(x.isPublic());
        dto.setCreator(x.getUser().getPrimaryOrcid() != null ? x.getUser().getPrimaryOrcid() : x.getUser().getUsername());
        dto.setCollaborators(x.getCollaborators().stream().map(c ->
            new CollectionCollaboratorDto(
                    c.getUser().getUsername(),
                    c.getUser().getPrimaryOrcid(),
                    c.getRole()
            )
        ).toList());
        dto.setTerminologies(x.getTerminologies().stream().map(r ->
            new ResourceDto(
                    r.getUri(),
                    r.getLabel(),
                    r.getSource(),
                    r.getType()
            )
        ).toList());
        dto.setStatistics(x.getStatistics().stream()
                .max(Comparator.comparing(CollectionStatistic::getTimestamp)
                        .thenComparing(CollectionStatistic::getId))
                .map(statistic -> List.of(CollectionStatisticDto.from(statistic)))
                .orElse(Collections.emptyList()));
        return dto;
    }
}

