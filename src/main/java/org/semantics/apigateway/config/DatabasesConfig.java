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
public class DatabasesConfig {
    private List<DatabaseConfig> databases;
}



