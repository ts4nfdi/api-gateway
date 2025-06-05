package org.semantics.apigateway.collections.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;


@Entity
@Getter @Setter
public class CollectionResource {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String uri;
    private String label;
    private String source;

    @Enumerated(EnumType.STRING)
    private CollectionResourceType type = CollectionResourceType.ARTEFACT;

    @ManyToOne
    @JoinColumn(name = "collection_id", nullable = false)
    @JsonIgnore
    private TerminologyCollection collection;
}
