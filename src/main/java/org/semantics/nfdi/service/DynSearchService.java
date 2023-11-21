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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        return apiKey.isEmpty() ? String.format(url, query) : String.format(url, query, apiKey);
    }

    public List<Map<String, Object>> filterResultsByFacets(List<Map<String, Object>> results, Map<String, String> selectedFacets) {
        return results.stream()
            .filter(result -> selectedFacets.entrySet().stream()
                .allMatch(facet -> result.containsKey(facet.getKey()) && result.get(facet.getKey()).equals(facet.getValue())))
            .collect(Collectors.toList());
    }

    @Async
    public CompletableFuture<List<Map<String, Object>>> search(String query, OntologyConfig config) {
        CompletableFuture<List<Map<String, Object>>> future = new CompletableFuture<>();

        String url = constructUrl(query, config);
        logger.info("Accessing URL: {}", url);

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                future.complete(dynTransformResponse.dynTransformResponse(response.getBody(), config));
            } else {
                future.complete(List.of());
            }
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        return future;
    }

    public CompletableFuture<Map<String, List<Map<String, Object>>>> performDynFederatedSearch(String query, String database) {
        Stream<OntologyConfig> filteredConfigs = ontologyConfigs.stream();
        if (database != null && !database.isEmpty()) {
            filteredConfigs = filteredConfigs.filter(config -> config.getOntology().equalsIgnoreCase(database));
        }

        ConcurrentMap<String, List<Map<String, Object>>> groupedResults = new ConcurrentHashMap<>();

        List<CompletableFuture<Void>> futures = filteredConfigs
                .map(config -> search(query, config)
                        .thenAccept(results -> groupedResults.put(config.getOntology(), results)))
                .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> groupedResults);
    }
    
}