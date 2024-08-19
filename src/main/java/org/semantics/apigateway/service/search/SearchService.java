package org.semantics.apigateway.service.search;

import lombok.Getter;
import org.apache.lucene.queryparser.classic.ParseException;
import org.semantics.apigateway.config.OntologyConfig;
import org.semantics.apigateway.model.DynTransformResponse;
import org.semantics.apigateway.service.ConfigurationLoader;
import org.semantics.apigateway.service.JsonLdTransform;
import org.semantics.apigateway.service.ResponseTransformerService;
import org.semantics.apigateway.service.ApiAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


@Service
public class SearchService {

    @Autowired
    private ConfigurationLoader configurationLoader;

    @Autowired
    private SearchLocalIndexerService localIndexer;

    @Autowired
    private ResponseTransformerService responseTransformerService;

    @Autowired
    @Getter
    private ApiAccessor accessor;

    @Autowired
    private JsonLdTransform jsonLdTransform;


    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);


    private final DynTransformResponse dynTransformResponse = new DynTransformResponse();

    private List<OntologyConfig> ontologyConfigs;

    public SearchService(ConfigurationLoader configurationLoader, SearchLocalIndexerService localIndexer) {
        this.configurationLoader = configurationLoader;
        this.ontologyConfigs = configurationLoader.getOntologyConfigs();
        this.localIndexer = localIndexer;
    }


    private boolean databaseExist(String database) {
        return database == null || database.isEmpty() ||
                ontologyConfigs.stream().anyMatch(config -> config.getDatabase().equalsIgnoreCase(database));
    }


    // Performs a federated search across multiple databases and optionally transforms the results for a target database schema
    public CompletableFuture<Object> performSearch(
            String query, String database, String format, String targetDbSchema) throws IOException, ParseException {

        CompletableFuture<Object> future = new CompletableFuture<>();

        if (!databaseExist(database)) {
            future.completeExceptionally(new IllegalArgumentException("Database not found: " + database));
            return future;
        }

        Map<String, String> apis = ontologyConfigs.stream()
                .collect(Collectors.toMap(OntologyConfig::getUrl, OntologyConfig::getApiKey));

        accessor.setUrls(apis);
        accessor.setLogger(logger);

        return accessor.get(query)
                .thenApply(data ->
                        data.entrySet().stream()
                                .map(entry -> {
                                    String url = entry.getKey();
                                    Map<String, Object> results = entry.getValue();
                                    OntologyConfig config = ontologyConfigs.stream()
                                            .filter(c -> c.getUrl().equalsIgnoreCase(url))
                                            .findFirst()
                                            .orElseThrow(() -> new RuntimeException("Config not found for URL: " + url));
                                    List<Map<String, Object>> transformedResponse = dynTransformResponse.dynTransformResponse(results, config);
                                    if ("jsonld".equalsIgnoreCase(format)) {
                                        transformedResponse = jsonLdTransform.convertToJsonLd(transformedResponse, config);
                                    }
                                    logger.info("Transformed API Response: {}", transformedResponse);
                                    return transformedResponse;
                                })
                                .collect(Collectors.toList())
                ).thenApply(data -> {
                    return data.stream().flatMap(List::stream).collect(Collectors.toList());
                }).thenApply(data -> {
                    try {
                        return this.localIndexer.reIndexResults(query.replace("*", ""), data, logger);
                    } catch (IOException | ParseException e) {
                        throw new RuntimeException(e);
                    }
                }).thenApply(data -> {
                    if (targetDbSchema != null && !targetDbSchema.isEmpty()) {
                        Object transformedResults = null;
                        try {
                            transformedResults = responseTransformerService.transformAndStructureResults(data, targetDbSchema);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        logger.info("Transformed results for database schema: {}", transformedResults);
                        return transformedResults;
                    } else {
                        return data;
                    }
                });
    }

}
