package org.semantics.apigateway.collections.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.semantics.apigateway.model.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter @Setter
public class TerminologyCollection {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String label;

    @OneToMany(mappedBy = "collection", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CollectionResource> terminologies = new ArrayList<>();

    @OneToMany(mappedBy = "collection", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CollectionCollaborator> collaborators = new ArrayList<>();

    @Column(nullable = false)
    private boolean isPublic;
}
