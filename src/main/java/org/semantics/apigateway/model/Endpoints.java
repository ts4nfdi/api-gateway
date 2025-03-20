package org.semantics.apigateway.model;

public enum Endpoints {
    resources,search, concepts, properties, collections, individuals,
    concept_details,resource_details, property_details, collection_details,individual_details, scheme_details;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}