package org.semantics.nfdi.config;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "search")
@Getter
@Setter
public class SearchConfiguration {

    private List<Ontology> ontologies;

    @Getter
    @Setter
    public static class Ontology {
        private String name;
        private String url;
        private String apiKey;
        private List<String> fields; 
    }
}
      