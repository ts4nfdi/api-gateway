package org.semantics.nfdi.config;


import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Component
public class DatabaseConfig {
    private List<OntologyConfig> databases;
    private Map<String, List<Map<String, String>>> mappings;

}



