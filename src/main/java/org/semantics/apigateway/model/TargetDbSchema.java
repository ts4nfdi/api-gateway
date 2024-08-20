package org.semantics.apigateway.model;

public enum TargetDbSchema {
    ONTOPORTAL,
    OLS,
    SKOSMOS;

    @Override
    public String toString() {
        return  this.name().toLowerCase();
    }
}
