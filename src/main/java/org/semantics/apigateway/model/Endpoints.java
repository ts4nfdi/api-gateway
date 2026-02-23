package org.semantics.apigateway.model;

/**
 * Enum representing the various endpoints available in the API Gateway.
 * Each endpoint corresponds to a specific resource or operation within the system.
 * The mapping to the actual endpoint paths is handled in the configuration
 * files in backend_types for each database: ontoportal, ...
 */
public enum Endpoints {
    resources,search, concepts, properties, collections, individuals, schemes,
    concept_details, resource_details, property_details, collection_details,individual_details, scheme_details,
    concepts_roots, concepts_children, concept_tree, suggest;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}