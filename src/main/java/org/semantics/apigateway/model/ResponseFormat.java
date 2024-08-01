package org.semantics.apigateway.model;

public enum ResponseFormat {
    JSON;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}