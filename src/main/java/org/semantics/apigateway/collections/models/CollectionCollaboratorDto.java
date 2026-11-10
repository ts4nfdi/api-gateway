package org.semantics.apigateway.collections.models;

import jakarta.validation.constraints.NotNull;
import org.semantics.apigateway.model.user.Role;

public record CollectionCollaboratorDto(
        @NotNull  String username,
        String orcid,
        Role role
) {
    public CollectionCollaboratorDto(String username, String orcid) {
        this(username, orcid, Role.USER);
    }
}


