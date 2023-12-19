package org.semantics.nfdi.config;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Component
public class DatabaseSchemaConfig {
    private Map<String, DatabaseConfig> databases;

    // Getters and setters
    public Map<String, DatabaseConfig> getDatabases() {
        return databases;
    }

    public void setDatabases(Map<String, DatabaseConfig> databases) {
        this.databases = databases;
    }
}
