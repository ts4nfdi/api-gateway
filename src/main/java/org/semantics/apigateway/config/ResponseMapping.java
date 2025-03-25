package org.semantics.apigateway.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.lang.reflect.Field;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResponseMapping {
    private String nestedJson;
    private String key;
    private String totalCount;
    private String page;

    @JsonIgnore
    private Class<?> mappedClass;

    private Map<String, String> mappedClassAttributes = new HashMap<>();

    private List<Field> getAllFields() {
        Field[] declaredFields = mappedClass.getDeclaredFields();
        Field[] parentFields = mappedClass.getSuperclass().getDeclaredFields();
        List<Field> allFields = new ArrayList<>();
        allFields.addAll(Arrays.asList(parentFields));
        allFields.addAll(Arrays.asList(declaredFields));
        return allFields;
    }

    public ResponseMapping fromMap(Map<String, String> mappingValues) {
        if(mappingValues == null) {
            return this;
        }

        List<Field> mappedClassFields = getAllFields();

        mappedClassFields.forEach(f -> {
            String fieldName = f.getName();
            mappedClassAttributes.put(fieldName, mappingValues.getOrDefault(fieldName, null));
        });

        this.setKey(mappingValues.getOrDefault("key", null));
        this.setNestedJson(mappingValues.getOrDefault("nestedJson", null));
        this.setPage(mappingValues.getOrDefault("page", null));
        this.setTotalCount(mappingValues.getOrDefault("totalCount", null));

        return this;
    }

    @JsonIgnore
    public Map<String, String> inverseMapping() {
        Map<String, String> inverseMapping = new HashMap<>();
        for (Map.Entry<String, String> entry : getMappedClassAttributes().entrySet()) {
            inverseMapping.put(entry.getValue(), entry.getKey());
        }

        inverseMapping.put(getTotalCount(), "totalCount");
        inverseMapping.put(getPage(), "page");
        inverseMapping.put(getNestedJson(), "nestedJson");
        inverseMapping.put(getKey(), "key");

        return inverseMapping;
    }

    @JsonIgnore
    public Map<String, String> toMap() {
        Map<String, String> mappingValues = mappedClassAttributes;
        mappingValues.put("totalCount", totalCount);
        mappingValues.put("page", page);
        mappingValues.put("nestedJson", nestedJson);
        mappingValues.put("key", key);
        return mappingValues;
    }
}