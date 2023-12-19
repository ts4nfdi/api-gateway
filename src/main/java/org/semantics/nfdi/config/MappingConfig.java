package org.semantics.nfdi.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Component
public class MappingConfig {
    private Map<String, DatabaseConfig> databases;
    private ResponseStructure responseStructure;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DatabaseConfig {
        private List<OntologyConfig> ontology;
    }
   
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OntologyConfig {
        private String database;
        private String url;
        private String apiKey;
        private ResponseMapping responseMapping;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseMapping {
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
        public boolean containsKey(String key2) {
            return false;
        }
        public String get(String key2) {
            return null;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseStructure {
        private Map<String, Object> topLevel;
        private Map<String, Object> staticFields;
    }
}
