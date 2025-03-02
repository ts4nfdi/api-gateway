package org.semantics.apigateway.service.configuration;

import org.semantics.apigateway.config.ResponseMapping;
import org.semantics.apigateway.model.BackendType;
import org.semantics.apigateway.model.Endpoints;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
public class MetadataService {
    private final ConfigurationLoader configurationLoader;

    public MetadataService(ConfigurationLoader configurationLoader) {
        this.configurationLoader = configurationLoader;
    }

    public Map<String, ResponseMapping> getMetadataMappings(Endpoints endpoint) {
        Map<String, ResponseMapping> responseMappings = new HashMap<>();
        Arrays.stream(BackendType.values())
                .forEach(backendType -> {
                    ResponseMapping rp = configurationLoader.getDatabaseConfig(backendType.toString())
                            .getResponseMapping(endpoint.toString());
                    responseMappings.put(backendType.toString(), rp);
                });
        return responseMappings;
    }

    public Map<String,ResponseMapping> getArtefactMetadata() {
        return getMetadataMappings(Endpoints.resources);
    }

    public Map<String,ResponseMapping> getTermMetadata() {
        return getMetadataMappings(Endpoints.concept_details);
    }

    public Map<String, ResponseMapping> getSearchMetadata() {
        return getMetadataMappings(Endpoints.concept_details);
    }
}
