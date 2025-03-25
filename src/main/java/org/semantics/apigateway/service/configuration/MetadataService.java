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


    public Map<String, Map<String, String>> getMetadataMappings(Endpoints endpoint) {
        Map<String, Map<String, String>> responseMappings = new HashMap<>();

        Arrays.stream(BackendType.values())
                .forEach(backendType -> {
                    ResponseMapping rp = configurationLoader.getDatabaseConfig(backendType.toString())
                            .getResponseMapping(endpoint.toString());
                    responseMappings.put(backendType.toString(), rp.toMap());
                });

        return responseMappings;
    }

    public Map<String, Map<String, String>> getArtefactMetadata() {
        return getMetadataMappings(Endpoints.resources);
    }

    public Map<String, Map<String, String>> getTermMetadata() {
        return getMetadataMappings(Endpoints.concept_details);
    }

    public Map<String, Map<String, String>> getSearchMetadata() {
        return getMetadataMappings(Endpoints.concept_details);
    }
}
