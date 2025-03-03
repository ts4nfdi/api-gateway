package org.semantics.apigateway.model;

public enum Endpoints {
    resources,search,concept_details,resource_details;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}