package org.semantics.apigateway.collections.models;

import jakarta.validation.constraints.NotNull;

public record ResourceDto(
        String uri,
        @NotNull String label,
        String source,
        CollectionResourceType type
) {
    public ResourceDto(String uri, String label, String source) {
        this(uri, label, source, CollectionResourceType.ARTEFACT);
    }
}
