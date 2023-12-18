package org.semantics.nfdi.service;

import org.semantics.nfdi.model.DatabaseTransform;
import org.semantics.nfdi.model.DynTransformResponse;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.semantics.nfdi.model.DynDatabaseTransform;
import com.github.jsonldjava.utils.JsonUtils;
import org.semantics.nfdi.config.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.semantics.nfdi.config.OntologyConfig;
import org.semantics.nfdi.config.ResponseMapping;


@Service
public class DynSearchService {

    @Value("classpath:config.yaml")
    private Resource dbConfigResource;
    private static final Logger logger = LoggerFactory.getLogger(DynSearchService.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final DynTransformResponse dynTransformResponse = new DynTransformResponse();
    private List<OntologyConfig> ontologyConfigs;
    private final DatabaseTransform databaseTransform = new DatabaseTransform();

    @Autowired
    private DynDatabaseTransform dynDatabaseTransform;

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
    public CompletableFuture<List<Map<String, Object>>> search(String query, OntologyConfig config, String format) {
        CompletableFuture<List<Map<String, Object>>> future = new CompletableFuture<>();
        try {
            String url = constructUrl(query, config);
            logger.info("Accessing URL: {}", url);
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Map<String, Object>> transformedResponse = dynTransformResponse.dynTransformResponse(response.getBody(), config);
    
                if ("jsonld".equalsIgnoreCase(format)) {
                    transformedResponse = convertToJsonLd(transformedResponse, config);
                }
    
                future.complete(transformedResponse);
            } else {
                future.complete(List.of());
            }
        } catch (Exception e) {
            logger.error("An error occurred while processing the request", e);
            future.completeExceptionally(e);
        }
        return future;
    }

    private List<Map<String, Object>> convertToJsonLd(List<Map<String, Object>> response, OntologyConfig config) {
        Map<String, Object> context = new HashMap<>();
        context.put("@vocab", "http://base4nfdi.de/ts4nfdi/schema/");
        context.put("ts", "http://base4nfdi.de/ts4nfdi/schema/");
        String type = "ts:Resource";
    
        ResponseMapping responseMapping = config.getResponseMapping();

        //    List<Map<String, Object>> graph = response.stream().map(item -> {
    
        return response.stream().map(item -> {
            try {
                Map<String, Object> jsonLd = new HashMap<>();
                jsonLd.put("@context", context);
                jsonLd.put("@type", type);
                // add key-value pairs to jsonLd
                for (Map.Entry<String, Object> entry : item.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
    
                    if (responseMapping.containsKey(key)) {
                        key = responseMapping.get(key);
                    }
    
                    jsonLd.put(key, value);
                }
                // convert
                String jsonString = JsonUtils.toString(jsonLd);
                if (jsonString != null) {
                    String jsonLdString = convertJsonToJsonLd(jsonString);
                    return (Map<String, Object>) JsonUtils.fromString(jsonLdString);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        }).collect(Collectors.toList());

        //Map<String, Object> result = new HashMap<>();
        //result.put("@context", context);
        //result.put("@graph", graph);
    
        //return result;
    }

    
    public static String convertJsonToJsonLd(String json) {
        Model model = ModelFactory.createDefaultModel();
        RDFDataMgr.read(model, new StringReader(json), null, Lang.JSONLD);

        StringWriter out = new StringWriter();
        RDFDataMgr.write(out, model, Lang.JSONLD);
        return out.toString();
    }

    public CompletableFuture<Object> performDynFederatedSearch(
            String query, String database, String format, boolean transformToDatabaseSchema) {

        boolean databaseExists = database != null && !database.isEmpty() &&
                ontologyConfigs.stream().anyMatch(config -> config.getDatabase().equalsIgnoreCase(database));

        if (!databaseExists && database != null && !database.isEmpty()) {
            CompletableFuture<Object> future = new CompletableFuture<>();
            future.completeExceptionally(new IllegalArgumentException("Database not found: " + database));
            return future;
        }

        Stream<OntologyConfig> configsStream = ontologyConfigs.stream();
        if (database != null && !database.isEmpty()) {
            configsStream = configsStream.filter(config -> database.equalsIgnoreCase(config.getDatabase()));
        }

        List<CompletableFuture<List<Map<String, Object>>>> futures = configsStream
                .map(config -> search(query, config, format))
                .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    List<Map<String, Object>> combinedResults = futures.stream()
                            .flatMap(resultFuture -> resultFuture.join().stream())
                            .collect(Collectors.toList());

                    if (transformToDatabaseSchema) {
                        return dynDatabaseTransform.transformDatabaseResponse(database, combinedResults);
                    } else {
                        return combinedResults;
                    }
                });
    }
    
}