package org.semantics.apigateway.model;

public enum RDFType {
    CLASSES, INDIVIDUALS, PROPERTIES, SCHEMES, COLLECTIONS;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}