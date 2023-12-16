package org.semantics.nfdi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "ols")
public class MappingConfig {
    private Map<String, String> mapping;
    private Map<String, Object> responseStructure;

    // Getters and setters
    public Map<String, String> getMapping() {
        return mapping;
    }

    public void setMapping(Map<String, String> mapping) {
        this.mapping = mapping;
    }

    public Map<String, Object> getResponseStructure() {
        return responseStructure;
    }

    public void setResponseStructure(Map<String, Object> responseStructure) {
        this.responseStructure = responseStructure;
    }
}
