package org.semantics.apigateway.config;


import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.List;

import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseConfig {
    private List<OntologyConfig> databases;
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DatabaseMapping {
        private String database;
        private ResponseMapping responseMapping;
    }
}



