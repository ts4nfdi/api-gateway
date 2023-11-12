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

    @Value("classpath:configuration.yaml")
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
            this.ontologyConfigs = dbConfig.getSearch().getOntologies();
            ontologyConfigs.forEach(config -> logger.info("Loaded config: {}", config));
        }
    }


    public CompletableFuture<List<Object>> performDynFederatedSearch(String query) {
        List<CompletableFuture<List<Object>>> futures = ontologyConfigs.stream()
                .map(config -> search(query, config))
                .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .flatMap(future -> future.join().stream())
                        .collect(Collectors.toList()));
    }

    @Async
    public CompletableFuture<List<Object>> search(String query, OntologyConfig config) {
        if (config == null || config.getUrl() == null) {
            logger.error("Configuration or URL is null");
            return CompletableFuture.completedFuture(List.of());
        }

        String url = constructUrl(query, config);

        logger.info("Accessing URL: {}", url);
        ResponseEntity<Map> response;
        try {
            response = restTemplate.getForEntity(url, Map.class);
        } catch (Exception e) {
            logger.error("Error accessing URL: {}", url, e);
            return CompletableFuture.completedFuture(List.of());
        }

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return CompletableFuture.completedFuture(dynTransformResponse.dynTransformResponse(response.getBody(), config));
        } else {
            logger.error("Unsuccessful response from URL: {}", url);
            return CompletableFuture.completedFuture(List.of());
        }
    }

    private String constructUrl(String query, OntologyConfig config) {
        String url = config.getUrl();
        if (url.contains("%s")) {
            url = String.format(url, query);
        } else {
            url += query;
        }

        return url;
    }
}
