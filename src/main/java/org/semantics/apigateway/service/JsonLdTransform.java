package org.semantics.apigateway.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.semantics.apigateway.model.ContextBaseUri;
import org.semantics.apigateway.model.ContextUri;
import org.semantics.apigateway.model.responses.AggregatedResourceBody;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class JsonLdTransform {
    public final static String DEFAULT_BASE_URI = "http://base4nfdi.de/ts4nfdi/schema/";

    public List<Map<String, Object>> convertToJsonLd(List<Map<String, Object>> response, String type, Map<String, String> context) {

        List<Map<String, Object>> nestedData = response;

        nestedData = nestedData.stream().peek(item -> {
            item.put("@context", context);
            item.put("@type", type); // TODO: make this a list
            item.put("@id", item.get("iri"));
        }).collect(Collectors.toList());

        return nestedData;
    }

    public Map<String, String> getNameSpaceMap() {
        return Map.of(
                "skos", "http://www.w3.org/2004/02/skos/core#",
                "dct", "http://purl.org/dc/terms/",
                "mod", "https://w3id.org/mod#",
                "owl", "http://www.w3.org/2002/07/owl#",
                "dcat", "http://www.w3.org/ns/dcat#",
                "pav", "http://purl.org/pav/",
                "schema", "http://schema.org/",
                "base4nfdi", "http://base4nfdi.de/ts4nfdi/schema/"
        );
    }


    public String getBaseUri() {
        ContextBaseUri baseUri = getClass().getAnnotation(ContextBaseUri.class);
        String baseUriString = DEFAULT_BASE_URI;
        if (baseUri != null) {
            baseUriString = getNameSpaceMap().getOrDefault(baseUri.value(), baseUri.value());
        }
        return baseUriString;
    }

    public String getTypeURI(Class<? extends AggregatedResourceBody> clazz) {
        AggregatedResourceBody instance;
        try {
            instance = clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            return null;
        }
        return instance.getTypeURI();
    }

    public Map<String, String> generateContext(Class<? extends AggregatedResourceBody> clazz, List<String> displayFields) {
        AggregatedResourceBody instance;
        try {
            instance = clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            return null;
        }

        Map<String, String> namespaceMap = getNameSpaceMap();
        Map<String, String> contextMap = new HashMap<>();
        String baseUriString = getBaseUri();

        contextMap.put("@base", baseUriString);

        for (Field field : instance.getAllFields()) {
            String fullUri;
            JsonProperty jsonProperty = field.getAnnotation(JsonProperty.class);
            String fieldName = jsonProperty != null ? jsonProperty.value() : field.getName();

            if (displayFields != null && !displayFields.isEmpty() && !displayFields.contains(field.getName()))
                continue;

            if (field.isAnnotationPresent(ContextUri.class)) {
                ContextUri annotation = field.getAnnotation(ContextUri.class);
                String uri = annotation.value();
                String[] uris = uri.split(":");
                fullUri = uri;
                if (uris.length > 1) {
                    String prefix = uris[0];
                    String namespace = namespaceMap.get(prefix);
                    if (namespace != null) {
                        fullUri = namespace + uris[1];
                    }
                } else {
                    String namespace = namespaceMap.get(uri);
                    if (namespace != null)
                        fullUri = namespace + fieldName;
                }

            } else {
                fullUri = baseUriString + fieldName;
            }
            contextMap.put(fieldName, fullUri);
        }
        return contextMap;
    }
}
