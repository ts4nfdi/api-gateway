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
public abstract class AggregatedResourceBody {
    @JsonIgnore
    private final Logger logger = LoggerFactory.getLogger(AggregatedResourceBody.class);
    @JsonIgnore
    protected Map<String, Object> originalBody;

    protected String iri;

    @JsonProperty("short_form")
    protected String shortForm;

    protected String source;

    @JsonProperty("source_name")
    protected String sourceName;

    @JsonProperty("source_url")
    protected String sourceUrl;

    @JsonProperty("@context")
    protected String context;
    @JsonProperty("@type")
    protected String typeURI;

    @JsonProperty("backend_type")
    protected String backendType;


    private void setFieldValue(Object target, Field field, Object value) {
        try {
            field.set(target, value);
        } catch (IllegalAccessException e) {
            logger.error(e.getMessage());
        }
    }

    private List<Field> getAllFields() {
        Field[] declaredFields = this.getClass().getDeclaredFields();
        Field[] parentFields = this.getClass().getSuperclass().getDeclaredFields();
        List<Field> allFields = new ArrayList<>();
        allFields.addAll(Arrays.asList(parentFields));
        allFields.addAll(Arrays.asList(declaredFields));
        return allFields;
    }

    public void fillWithItem(Map<String, Object> item, ResponseMapping mapping) {
        AggregatedResourceBody target = this;
        // Create a reflection-based mapping of getter methods from ResponseMapping to their values
        Map<String, String> responseMappings = mapping.toMap();


        for (Field field : this.getAllFields()) {
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
                e.printStackTrace();
                logger.error("Filling aggregated body error {}", e.getMessage());
            }
        }
    }

    public static <T extends AggregatedResourceBody> T fromMap(
            Map<String, Object> item,
            DatabaseConfig config,
            String endpoint, T object) throws RuntimeException {
        ResponseMapping responseMapping = config.getResponseMapping(endpoint);

        object.setOriginalBody(item);
        object.fillWithItem(item, responseMapping);
        object.setDefaultValues(config);

        if (item.containsKey("@context")) {
            object.setContext(item.get("@context").toString());
        }
        if (item.containsKey("@type")) {
            object.setTypeURI(item.get("@type").toString());
        }

        return object;
    }

    public void setDefaultValues(DatabaseConfig config) {
        AggregatedResourceBody newItem = this;
        if (newItem.getShortForm() == null || newItem.getShortForm().isEmpty()) {
            String iri = newItem.getIri();
            if (iri.contains("#")) {
                newItem.setShortForm(iri.substring(iri.lastIndexOf("#") + 1));
            } else if (iri.contains("/")) {
                newItem.setShortForm(iri.substring(iri.lastIndexOf("/") + 1));
            } else {
                newItem.setShortForm(newItem.getIri());
            }
        }

        newItem.setSource(config.getUrl());
        newItem.setBackendType(config.getDatabase());
        newItem.setSourceName(config.getName());
    }

    public Map<String, Object> toMap(boolean includeOriginalBody, boolean displayEmpty) {
        Map<String, Object> map = new HashMap<>();

        for (Field field : this.getAllFields()) {
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
        Object value = MappingTransformer.itemValueGetter(item, key);
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
        Object value = MappingTransformer.itemValueGetter(item, key);
        List<String> list = Collections.emptyList();
        if (value instanceof List) {
            list = (List<String>) value;
        } else if (value != null) {
            list = List.of(String.valueOf(value));
        }
        setter.accept(list);
    }
}
