package org.semantics.apigateway.config;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.semantics.apigateway.model.Endpoints;
import org.semantics.apigateway.model.RDFResource;
import org.semantics.apigateway.model.SemanticArtefact;
import org.springframework.stereotype.Component;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Component
public class DatabaseConfig {
    private String type;
    private String name;
    private String url;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String apiKey;
    @JsonIgnore
    private ServiceConfig serviceConfig;

    public ResponseMapping getResponseMapping(String endpoint) {
        Class<?> mappedClass = getMappingClass(Endpoints.valueOf(endpoint));
        Map<String, String> out = serviceConfig.getEndpoints().getOrDefault(endpoint, new EndpointConfig()).getResponseMapping();
        ResponseMapping responseMapping = new ResponseMapping();
        responseMapping.setMappedClass(mappedClass);
        return responseMapping.fromMap(out);
    }

    public static Class<?> getMappingClass(Endpoints endpoint) {
        return switch (endpoint) {
            case resources, resource_details -> SemanticArtefact.class;
            case concept_details, search -> RDFResource.class;
        };
    }

    @JsonIgnore
    public String getDatabase() {
        return type;
    }

    public String getApiKey() {
        return apiKey == null ? "" : apiKey;
    }

    public String getUrl(String endpoint) {
        String path = serviceConfig.getEndpoints().getOrDefault(endpoint, new EndpointConfig()).getPath();
        String url = this.url;

        if (url != null && url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }

        if (path != null && path.startsWith("/")) {
            path = path.substring(1);
        }

        return this.url + '/' + path;
    }

    public String getSearchUrl() {
        return getUrl("search");
    }

    public String getArtefactsUrl() {
        return getUrl("resources");
    }

}
