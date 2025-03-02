package org.semantics.apigateway.model.responses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.semantics.apigateway.config.DatabaseConfig;
import org.semantics.apigateway.config.ResponseMapping;

import java.util.*;
import java.util.function.Consumer;

@Getter
@Setter
@NoArgsConstructor
public class AggregatedResourceBody {
    private String iri;
    private String label;
    private List<String> synonyms;
    private List<String> descriptions;
    @JsonProperty("short_form")
    private String shortForm;
    private String ontology;
    @JsonProperty("ontology_iri")
    private String ontologyIri;
    private String type;

    private String source;
    @JsonProperty("source_name")
    private String sourceName;
    @JsonProperty("source_url")
    private String sourceUrl;

    @JsonProperty("backend_type")
    private String backendType;

    private String created;
    private String modified;
    private String version;

    @JsonProperty("@context")
    private String context;
    @JsonProperty("@type")
    private String typeURI;

    private boolean obsolete;

    @JsonIgnore
    private Map<String, Object> originalBody;

    public static AggregatedResourceBody fromMap(Map<String, Object> item, DatabaseConfig config, String endpoint) throws RuntimeException {
        AggregatedResourceBody newItem = new AggregatedResourceBody();
        ResponseMapping responseMapping = config.getResponseMapping(endpoint);
        newItem.setOriginalBody(item);

        // Mapping fields based on the JSON configuration
        try {
            if (responseMapping.getIri() != null && item.containsKey(responseMapping.getIri())) {
                newItem.setIri((String) item.get(responseMapping.getIri()));
            }
            if (responseMapping.getLabel() != null && item.containsKey(responseMapping.getLabel())) {
                if (item.get(responseMapping.getLabel()) instanceof ArrayList) {
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

            if (responseMapping.getDescription() != null) {
                Object label = itemValueGetter(item, responseMapping.getDescription());
                if (label instanceof List) {
                    newItem.setDescription((List<String>) label);
                } else if (label != null) {
                    newItem.setDescription(List.of(label.toString()));
                }
            }
            if (responseMapping.getOntology() != null && item.containsKey(responseMapping.getOntology())) {
                if (responseMapping.getOntology().equals("links")) {
                    Object keysObject = ((Map<?, ?>) item).get(responseMapping.getOntology());
                    String ontologyItem = ((Map<?, String>) keysObject).get("ontology");
                    if (ontologyItem != null) {
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

            if (responseMapping.getSourceUrl() != null) {
                Object value = itemValueGetter(item, responseMapping.getSourceUrl());
                if (value != null) {
                    newItem.setSourceUrl(value.toString());
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


    public Map<String, Object> toMap(boolean includeOriginalBody) {
        Map<String, Object> map = new HashMap<>();

        putIfNotEmpty(map, "iri", this.iri);
        putIfNotEmpty(map, "label", this.label);
        putIfNotEmpty(map, "synonyms", this.synonyms);
        putIfNotEmpty(map, "descriptions", this.descriptions);
        putIfNotEmpty(map, "short_form", this.shortForm);
        putIfNotEmpty(map, "type", this.type);
        putIfNotEmpty(map, "source", this.source);
        putIfNotEmpty(map, "source_name", this.sourceName);
        putIfNotEmpty(map, "source_url", this.sourceUrl);
        putIfNotEmpty(map, "backend_type", this.backendType);
        putIfNotEmpty(map, "ontology", this.ontology);
        putIfNotEmpty(map, "obsolete", this.obsolete);
        putIfNotEmpty(map, "ontology_iri", this.ontologyIri);
        putIfNotEmpty(map, "created", this.created);
        putIfNotEmpty(map, "modified", this.modified);
        putIfNotEmpty(map, "version", this.version);

        if (includeOriginalBody)
            map.put("originalResponse", this.originalBody);

        return map;
    }


    private void putIfNotEmpty(Map<String, Object> map, String key, Object value) {
        if (value != null && !(value instanceof String && value.toString().isEmpty())) {
            map.put(key, value);
        }
    }

    private static Object itemValueGetter(Map<String, Object> item, String key) {

        if (key.contains("->")) {
            String[] keys = key.split("->");
            Object value = item;
            for (String s : keys) {

                if(value == null) {
                    break;
                }

                if(value instanceof Map){
                    value = ((Map<?, ?>) value).get(s);
                }
            }

            return value;
        } else if (item.containsKey(key)) {
            return item.get(key);
        }
        return null;
    }
}
