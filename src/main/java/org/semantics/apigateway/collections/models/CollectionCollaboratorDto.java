package org.semantics.apigateway.collections.models;

import jakarta.validation.constraints.NotNull;
import org.semantics.apigateway.model.user.Role;

public record CollectionCollaboratorDto(
        @NotNull  String username,
        Role role
) {
    public CollectionCollaboratorDto(String username) {
        this(username, Role.USER);
    }
}


