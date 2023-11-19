package org.semantics.nfdi.service;

import org.semantics.nfdi.model.DynTransformResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.semantics.nfdi.config.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.semantics.nfdi.config.OntologyConfig;

@Service
public class DynSearchService extends SearchService {

    @Value("classpath:config.yaml")
    private Resource dbConfigResource;
    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final DynTransformResponse dynTransformResponse = new DynTransformResponse();
    private List<OntologyConfig> ontologyConfigs;

    @PostConstruct
    public void loadDbConfigs() throws IOException {
        Yaml yaml = new Yaml(new Constructor(DatabaseConfig.class));
        try (InputStream in = dbConfigResource.getInputStream()) {
            DatabaseConfig dbConfig = yaml.loadAs(in, DatabaseConfig.class);
            this.ontologyConfigs = dbConfig.getDatabases();
            ontologyConfigs.forEach(config -> logger.info("Loaded config: {}", config));
        }
    }

    private String constructUrl(String query, OntologyConfig config) {
        String url = config.getUrl();
        String apiKey = config.getApiKey();
    
        if (url.contains("%s")) {
            url = apiKey.isEmpty() ? String.format(url, query) : String.format(url, query, apiKey);
        } else {
            url += query;
        }
    
        return url;
    }
    
    @Async
    public CompletableFuture<List<Map<String, Object>>> search(String query, OntologyConfig config) {
        CompletableFuture<List<Map<String, Object>>> future = new CompletableFuture<>();

        if (config == null || config.getUrl() == null) {
            logger.error("Configuration or URL is null");
            future.complete(List.of());
            return future;
        }

        String url = constructUrl(query, config);
        logger.info("Accessing URL: {}", url);

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                future.complete(dynTransformResponse.dynTransformResponse(response.getBody(), config));
            } else {
                logger.error("Unsuccessful or empty response from URL: {}", url);
                future.complete(List.of());
            }
        } catch (Exception e) {
            logger.error("Error accessing URL: {}", url, e);
            future.completeExceptionally(e);
        }

        return future;
    }

    public CompletableFuture<List<Map<String, Object>>> performDynFederatedSearch(String query) {
        List<CompletableFuture<List<Map<String, Object>>>> futures = ontologyConfigs.stream()
                .map(config -> search(query, config))
                .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .flatMap(future -> future.join().stream())
                        .collect(Collectors.toList()));
    }
}
