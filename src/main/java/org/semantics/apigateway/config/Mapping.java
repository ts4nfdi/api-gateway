package org.semantics.apigateway.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Component
public class Mapping {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public class DatabaseConfig {
        private List<OntologyConfig> databases;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public class OntologyConfig {
        private String database;
        private String url;
        private String apiKey;
        private ResponseMapping responseMapping;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public class ResponseMapping {
        private String nestedJson;
        private String iri;
        private String label;
        private String source;
        private String synonym;
        private String ontology;
        private String key;
        private String description;

        public List<String> getFieldList() {
            return Arrays.asList(iri, label, source, synonym, ontology, description);
        }

        public String getFieldName(String field) {
            switch (field) {
                case "iri":
                    return this.iri;
                case "label":
                    return this.label;
                case "source":
                    return this.source;
                case "synonym":
                    return this.synonym;
                case "ontology":
                    return this.ontology;
                default:
                    return field;
            }
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public class Search {
        private List<OntologyConfig> ontology;
    }
}
