package org.semantics.apigateway.model.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jsonldjava.utils.Obj;
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
    @JsonProperty("source_url")
    private String sourceUrl;
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

        setStringProperty(item, responseMapping.getIri(), newItem::setIri);
        setStringProperty(item, responseMapping.getLabel(), newItem::setLabel);
        setListProperty(item, responseMapping.getSynonym(), newItem::setSynonyms);
        setListProperty(item, responseMapping.getDescription(), newItem::setDescriptions);
        setStringProperty(item, responseMapping.getShortForm(), newItem::setShortForm);
        setStringProperty(item, responseMapping.getVersion(), newItem::setVersion);
        setStringProperty(item, responseMapping.getType(), newItem::setType);
        setStringProperty(item, responseMapping.getSourceUrl(), newItem::setSourceUrl);
        setBooleanProperty(item, responseMapping.getObsolete(), newItem::setObsolete);
        setStringProperty(item, responseMapping.getOntologyIri(), newItem::setOntologyIri);
        setStringProperty(item, responseMapping.getOntology(), newItem::setOntology);
        setStringProperty(item, responseMapping.getModified(), newItem::setModified);
        setStringProperty(item, responseMapping.getCreated(), newItem::setCreated);


        if (item.containsKey("@context")) {
            newItem.setContext(item.get("@context").toString());
        }

        if (item.containsKey("@type")) {
            newItem.setTypeURI(item.get("@type").toString());
        }


        newItem.setSource(config.getUrl());
        newItem.setBackendType(config.getDatabase());
        newItem.setSourceName(config.getName());


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
        putIfNotEmpty(map, "source_url", this.sourceUrl);
        putIfNotEmpty(map, "backend_type", this.backendType);
        putIfNotEmpty(map, "ontology", this.ontology);

        return map;
    }


    private void putIfNotEmpty(Map<String, Object> map, String key, Object value) {
        map.put(key, value);
    }

    private static void setStringProperty(Map<String, Object> item, String key, Consumer<String> setter) {
        Object value = itemValueGetter(item, key);
        Optional.ofNullable(value)
                .map(x -> {
                    if (x instanceof List) {
                        return ((List<?>) x).get(0);
                    } else {
                        return x;
                    }
                }).map(Object::toString).ifPresent(setter);
    }

    private static void setBooleanProperty(Map<String, Object> item, String key, Consumer<Boolean> setter) {
        Object value = itemValueGetter(item, key);
        if (value != null) {
            setter.accept(Boolean.parseBoolean(value.toString()));
        } else {
            setter.accept(false);
        }
    }

    private static void setListProperty(Map<String, Object> item, String key, Consumer<List<String>> setter) {
        Object value = itemValueGetter(item, key);
        List<String> list = Collections.emptyList();
        if (value instanceof List) {
            list = (List<String>) value;
        } else if (value != null) {
            list = List.of(String.valueOf(value));
        }
        setter.accept(list);
    }

    private static Object itemValueGetter(Map<String, Object> item, String key) {
        if (key == null) {
            return null;
        }

        String[] options = key.split("\\|");

        // Use findFirst to return the first non-null value found
        return Arrays.stream(options)
                .map(option -> {
                    if (option.contains("->")) {
                        String[] keys = option.split("->");
                        Object value = item;
                        for (String s : keys) {
                            if (value == null) {
                                break;
                            }
                            if (value instanceof Map) {
                                value = ((Map<?, ?>) value).get(s);
                            } else if (value instanceof List) {
                                value = listItemValueGetter(s, value);
                            }
                        }
                        return value;
                    } else if (item.containsKey(option)) {
                        // This should use 'option' not 'key'
                        return item.get(option);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private static List<Object> listItemValueGetter(String key, Object value) {
        List<?> list = (List<?>) value;
        if (list.isEmpty()) {
            return null;
        }

        List<Object> out = new ArrayList<>();

        list.forEach(x -> {
            if (x instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) x;
                out.add(map.get(key));
            }
        });
        return out;
    }
}
