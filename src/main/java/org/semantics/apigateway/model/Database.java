package org.semantics.apigateway.model;

public enum Database {
    ONTOPORTAL,
    OLS,
    SKOSMOS;

    @Override
    public String toString() {
        return  this.name().toLowerCase();
    }
}
