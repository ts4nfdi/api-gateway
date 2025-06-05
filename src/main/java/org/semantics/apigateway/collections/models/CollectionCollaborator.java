package org.semantics.apigateway.collections.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.semantics.apigateway.model.user.Role;
import org.semantics.apigateway.model.user.User;

@Entity
@Getter
@Setter
public class CollectionCollaborator {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JsonIgnore
    private TerminologyCollection collection;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private User user;

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;
}
