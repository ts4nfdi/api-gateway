package org.semantics.apigateway.model.responses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.semantics.apigateway.config.DatabaseConfig;
import org.semantics.apigateway.config.ResponseMapping;
import org.semantics.apigateway.service.MappingTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;

@Getter
@Setter
@NoArgsConstructor
public class AggregatedResourceBody {
    @JsonIgnore
    private final Logger logger = LoggerFactory.getLogger(AggregatedResourceBody.class);
    @JsonIgnore
    private boolean localDataValues = false;

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
    private String status;
    private String versionIRI;
    private String accessRights;
    private String license;
    private String identifier;
    private List<String> keywords;
    private String landingPage;
    private List<String> language;
    private List<String> subject;
    private String accrualMethod;
    private String accrualPeriodicity;

    private List<String> bibliographicCitation;

    private List<String> contactPoint;
    private List<String> creator;
    private List<String> contributor;
    private List<String> rightsHolder;
    private List<String> publisher;

    private String coverage;
    private List<String> hasFormat;
    private String competencyQuestion;
    private String semanticArtefactRelation;
    private List<String> createdWith;
    private List<String> wasGeneratedBy;

    //TODO use this instead of source in the future
    private List<String> includedInDataCatalog;

    @JsonIgnore
    private Map<String, Object> originalBody;

    private void setFieldValue(Object target, Field field, Object value) {
        try {
            field.set(target, value);
        } catch (IllegalAccessException e) {
            logger.error(e.getMessage());
        }
    }

    private void fillWithItem(Map<String, Object> item, ResponseMapping mapping, boolean hardcodedValuesConfig) {
        AggregatedResourceBody target = this;
        // Create a reflection-based mapping of getter methods from ResponseMapping to their values
        Map<String, String> responseMappings = mapping.toMap();
        setLocalDataValues(hardcodedValuesConfig);

        Field[] fields = this.getClass().getDeclaredFields();

        for (Field field : fields) {
            String fieldName = field.getName();

            // Skip fields that we don't want to map automatically
            if (fieldName.equals("originalBody") ||
                    fieldName.equals("source") ||
                    fieldName.equals("backendType") ||
                    fieldName.equals("sourceName") ||
                    fieldName.equals("context") ||
                    fieldName.equals("typeURI")) {
                continue;
            }


            String mappingValue = responseMappings.get(fieldName);


            if (mappingValue == null) {
                continue; // No mapping found for this field
            }

            try {
                field.setAccessible(true);
                Class<?> fieldType = field.getType();

                if (fieldType == String.class) {
                    setStringProperty(item, mappingValue,
                            value -> setFieldValue(target, field, value));
                } else if (fieldType == boolean.class || fieldType == Boolean.class) {
                    setBooleanProperty(item, mappingValue,
                            value -> setFieldValue(target, field, value));
                } else if (List.class.isAssignableFrom(fieldType)) {
                    setListProperty(item, mappingValue,
                            value -> setFieldValue(target, field, value));
                }
            } catch (Exception e) {
                logger.error("Filling aggregated body error {}", e.getMessage());
            }
        }
    }

    public static AggregatedResourceBody fromMap(Map<String, Object> item, DatabaseConfig config, String endpoint) throws RuntimeException {
        AggregatedResourceBody newItem = new AggregatedResourceBody();
        ResponseMapping responseMapping = config.getResponseMapping(endpoint);
        newItem.setOriginalBody(item);

        boolean localDataValues = config.getUrl(endpoint).endsWith("localData");
        newItem.fillWithItem(item, responseMapping, localDataValues);

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

    public Map<String, Object> toMap(boolean includeOriginalBody, boolean displayEmpty) {
        Map<String, Object> map = new HashMap<>();

        for (Field field : this.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);

                String propertyName = field.getName();

                if (propertyName.equals("context") || propertyName.equals("typeURI")) {
                    continue;
                }

                if (!includeOriginalBody && field.getName().equals("originalBody")) {
                    continue;
                }

                JsonProperty jsonProperty = field.getAnnotation(JsonProperty.class);
                JsonIgnore jsonIgnore = field.getAnnotation(JsonIgnore.class);

                if (jsonIgnore != null) {
                    continue;
                }

                if (jsonProperty != null) {
                    propertyName = jsonProperty.value();
                }


                // Add to map if not null
                Object value = field.get(this);

                if (isEmpty(value) && !displayEmpty) {
                    continue;
                }

                map.put(propertyName, value);
            } catch (IllegalAccessException ignored) {
            }
        }

        if (includeOriginalBody) {
            map.put("originalResponse", originalBody);
        }

        return map;
    }


    public static boolean isEmpty(Object value) {
        return value == null ||
                value.equals("") ||
                (value instanceof List && ((List<?>) value).isEmpty());
    }

    private void setStringProperty(Map<String, Object> item, String key, Consumer<String> setter) {
        Object value;
        if (localDataValues) {
            value = key;
        } else {
            value = MappingTransformer.itemValueGetter(item, key);

        }
        Optional.ofNullable(value)
                .map(x -> {
                    if (x instanceof List<?> list) {
                        return list.isEmpty() ? null : list.get(0);
                    } else {
                        return x;
                    }
                }).map(Object::toString).ifPresent(setter);
    }

    private void setBooleanProperty(Map<String, Object> item, String key, Consumer<Boolean> setter) {
        Object value = MappingTransformer.itemValueGetter(item, key);
        if (value != null) {
            setter.accept(Boolean.parseBoolean(value.toString()));
        } else {
            setter.accept(false);
        }
    }

    private void setListProperty(Map<String, Object> item, String key, Consumer<List<String>> setter) {
        Object value;
        if (localDataValues) {
            value = key != null ? List.of(key.split(";")) : Collections.emptyList();
        } else {
            value = MappingTransformer.itemValueGetter(item, key);
        }
        List<String> list = Collections.emptyList();
        if (value instanceof List) {
            list = (List<String>) value;
        } else if (value != null) {
            list = List.of(String.valueOf(value));
        }
        setter.accept(list);
    }


}
