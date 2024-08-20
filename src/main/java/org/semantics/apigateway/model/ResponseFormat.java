package org.semantics.apigateway.model;

public enum ResponseFormat {
    JSON,
    JSONLD;

    @Override
    public String toString() {
        return  this.name().toLowerCase();
    }
}
