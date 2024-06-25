package org.semantics.apigateway.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

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

    public List<String> getFieldList(String schemaFieldName) {
        return Arrays.asList(iri, label, source, backendType, shortForm, synonym, ontology, description, type);
    }

    public String getFieldName(String field) {
        switch (field) {
            case "iri":
                return (iri != null && !iri.isEmpty()) ? iri : "0";
            case "label":
                return (label != null && !label.isEmpty()) ? label : "0";
            case "source":
                return (source != null && !source.isEmpty()) ? source : "0";
            case "backend_type":
                return (backendType != null && !backendType.isEmpty()) ? backendType : "0";
            case "short_form":
                return (shortForm != null && !shortForm.isEmpty()) ? shortForm : "0";
            case "synonym":
                return (synonym != null && !synonym.isEmpty()) ? synonym : "0";
            case "ontology":
                return (ontology != null && !ontology.isEmpty()) ? ontology : "0";
            case "description":
                return (description != null && !description.isEmpty()) ? description : "0";
            case "type":
                return (type != null && !type.isEmpty()) ? type : "0";
            default:
                return "0";
        }
    }

    public String getBackMapping(String field) {
        switch (field) {
            case "iri":
                return (iri != null && !iri.isEmpty()) ? iri : "0";
            case "label":
                return (label != null && !label.isEmpty()) ? label : "0";
            case "source":
                return (source != null && !source.isEmpty()) ? source : "0";
            case "backend_type":
                return (backendType != null && !backendType.isEmpty()) ? backendType : "0";
            case "short_form":
                return (shortForm != null && !shortForm.isEmpty()) ? shortForm : "0";
            case "synonym":
                return (synonym != null && !synonym.isEmpty()) ? synonym : "0";
            case "ontology":
                return (ontology != null && !ontology.isEmpty()) ? ontology : "0";
            case "description":
                return (description != null && !description.isEmpty()) ? description : "0";
            case "type":
                return (type != null && !type.isEmpty()) ? type : "0";
            default:
                return "0";
        }
    }

    public boolean containsKey(String key2) {
        return false;
    }

    public String get(String key2) {
        return null;
    }

    public List<String> getFieldListBack() {
        return Arrays.asList(iri, label, source, backendType, shortForm, synonym, ontology, description, type);
    }
}