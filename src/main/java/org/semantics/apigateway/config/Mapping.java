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
        private String backendType;
        private String shortForm;
        private String synonym;
        private String ontology;
        private String key;
        private String description;
        private String type;
        private String scopeNote;
        private String language;

        public List<String> getFieldList() {
            return Arrays.asList(iri, label, source, backendType, shortForm, synonym, ontology, description, type, scopeNote, language );
        }

        public String getFieldName(String field) {
            switch (field) {
                case "iri":
                    return this.iri;
                case "label":
                    return this.label;
                case "source":
                    return this.source;
                case "backend_type":
                    return this.backendType;
                case "short_form":
                    return this.shortForm;
                case "synonym":
                    return this.synonym;
                case "ontology":
                    return this.ontology;
                case "type":
                    return this.type;
                case "language":
                    return this.language;
                case "scopeNote":
                    return this.scopeNote;
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
