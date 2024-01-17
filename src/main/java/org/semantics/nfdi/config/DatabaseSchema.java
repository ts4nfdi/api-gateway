package org.semantics.nfdi.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@Component
public class DatabaseConfig {
    private Map<String, DatabaseMapping> databasesconfig;
}
