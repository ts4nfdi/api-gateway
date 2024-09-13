package org.semantics.apigateway.config;


import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
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
    private String apiKey;

    private ServiceConfig serviceConfig;


    public ResponseMapping getResponseMapping(String endpoint){
        return serviceConfig.getEndpoints().getOrDefault(endpoint, new EndpointConfig()).getResponseMapping();
    }

    public String getDatabase(){
        return type;
    }

    public String getApiKey() {
        return apiKey == null ? "" : apiKey;
    }

    public String getUrl(String endpoint) {
        String path = serviceConfig.getEndpoints().getOrDefault(endpoint, new EndpointConfig()).getPath();

        return this.url + "/" + path;
    }

    public String getSearchUrl() {
        return getUrl("search");
    }

    public String getArtefactsUrl() {
        return getUrl("resources");
    }
}
