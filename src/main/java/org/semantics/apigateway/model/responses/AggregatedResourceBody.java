package org.semantics.apigateway.model.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.jena.rdf.model.ResourceFactory;
import org.semantics.apigateway.config.DatabaseConfig;
import org.semantics.apigateway.config.ResponseMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class AggregatedResourceBody {
    private String iri;
    private String label;
    private List<String> synonym;
    private List<String> description;
    @JsonProperty("short_form")
    private String shortForm;
    private String ontology;
    private String type;
    private String source;
    @JsonProperty("source_name")
    private String sourceName;
    @JsonProperty("backend_type")
    private String backendType;

    @JsonProperty("@context")
    private String context;
    @JsonProperty("@type")
    private String typeURI;

    public static AggregatedResourceBody fromMap(Map<String, Object> item, DatabaseConfig config, String endpoint) throws RuntimeException {
        AggregatedResourceBody newItem = new AggregatedResourceBody();
        ResponseMapping responseMapping = config.getResponseMapping(endpoint);

        // Mapping fields based on the JSON configuration
        try {
            if (responseMapping.getIri() != null && item.containsKey(responseMapping.getIri())) {
                newItem.setIri((String) item.get(responseMapping.getIri()));
            }
            if (responseMapping.getLabel() != null && item.containsKey(responseMapping.getLabel())) {
                if (item.get(responseMapping.getLabel()) instanceof ArrayList){
                    newItem.setLabel((String) ((ArrayList<?>) item.get(responseMapping.getLabel())).get(0));
                } else {
                    newItem.setLabel(item.get(responseMapping.getLabel()).toString());
                }
            }
            if (responseMapping.getSynonym() != null && item.containsKey(responseMapping.getSynonym())) {
                Object label = item.get(responseMapping.getSynonym());
                if (label instanceof List) {
                    newItem.setSynonym((List<String>) label);
                } else {
                    newItem.setSynonym(List.of(label.toString()));
                }
            }

            if (responseMapping.getShortForm() != null && item.containsKey(responseMapping.getShortForm())) {
                newItem.setShortForm((String) item.get(responseMapping.getShortForm()));
            } else if (newItem.getIri() != null) {
                newItem.setShortForm(
                        ResourceFactory.createResource(newItem.getIri()).getLocalName().toLowerCase());
            }

            if (responseMapping.getDescription() != null && item.containsKey(responseMapping.getDescription())) {
                Object label = item.get(responseMapping.getDescription());
                if (label instanceof List) {
                    newItem.setDescription((List<String>) label);
                } else {
                    newItem.setDescription(List.of(label.toString()));
                }
            }
            if (responseMapping.getOntology() != null && item.containsKey(responseMapping.getOntology())) {
                if (responseMapping.getOntology().equals("links")) {
                    Object keysObject = ((Map<?, ?>) item).get(responseMapping.getOntology());
                    String ontologyItem = ((Map<?, String>) keysObject).get("ontology");
                    if(ontologyItem != null) {
                        newItem.setOntology(ResourceFactory.createResource(ontologyItem).getLocalName().toLowerCase());
                    }
                } else {
                    newItem.setOntology((String) item.get(responseMapping.getOntology()));
                }
            }
            if (responseMapping.getType() != null && item.containsKey(responseMapping.getType())) {
                if (config.getDatabase().equals("ontoportal")) {
                    newItem.setType("class");
                    // ontoportal do the search only on classes for now
                } else if (config.getDatabase().equals("skosmos")) {
                    newItem.setType("individual");
                    // workaround ols type implementation that do not support skos types
                } else {
                    newItem.setType((String) item.get(responseMapping.getType()));
                }
            }

            // Adding the source database as part of the new item
            if (String.valueOf(config.getUrl()).contains("/search?")) {
                newItem.setSource(String.valueOf(config.getUrl()).substring(0, String.valueOf(config.getUrl()).indexOf("/search?")));
            } else if (String.valueOf(config.getUrl()).contains("/select?")) {
                newItem.setSource(String.valueOf(config.getUrl()).substring(0, String.valueOf(config.getUrl()).indexOf("/select?")));
            } else {
                newItem.setSource(config.getUrl());
            }

            if (item.containsKey("@context")) {
                newItem.setContext(item.get("@context").toString());
            }

            if (item.containsKey("@type")) {
                newItem.setTypeURI(item.get("@type").toString());
            }

            // Adding the backend database type as part of the new item
            newItem.setBackendType(config.getDatabase());

            newItem.setSourceName(config.getName());

        } catch (RuntimeException e) {
            throw e;
        }
        // logger.info("Transformed item: {}", newItem);
        return newItem;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();

        putIfNotEmpty(map, "iri", this.iri);
        putIfNotEmpty(map, "label", this.label);
        putIfNotEmpty(map, "synonym", this.synonym);
        putIfNotEmpty(map, "description", this.description);
        putIfNotEmpty(map, "short_form", this.shortForm);
        putIfNotEmpty(map, "type", this.type);
        putIfNotEmpty(map, "source", this.source);
        putIfNotEmpty(map, "source_name", this.sourceName);
        putIfNotEmpty(map, "backend_type", this.backendType);
        putIfNotEmpty(map, "ontology", this.ontology);

        return map;
    }


    private void putIfNotEmpty(Map<String, Object> map, String key, Object value) {
        if (value != null && !(value instanceof String && value.toString().isEmpty())) {
            map.put(key, value);
        }
    }
}
