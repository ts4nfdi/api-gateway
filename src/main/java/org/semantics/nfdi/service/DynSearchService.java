package org.semantics.nfdi.service;

import org.semantics.nfdi.config.MappingConfig;
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

    @Value("classpath:databaseSchema.yaml")
    private Resource dbSchemaConfigResource;
    private final RestTemplate restTemplate = new RestTemplate();
    private final DynTransformResponse dynTransformResponse = new DynTransformResponse();
    private static final Logger logger = LoggerFactory.getLogger(DynSearchService.class);
    private List<MappingConfig.OntologyConfig> ontologyConfigs; // Use OntologyConfig from MappingConfig
    private Map<String, Object> dbSchemaConfig;

    @Autowired
    private DynDatabaseTransform dynDatabaseTransform;

    @PostConstruct
    public void loadDbConfigs() throws IOException {
        Yaml yaml = new Yaml(new Constructor(MappingConfig.class));
        try (InputStream in = dbConfigResource.getInputStream()) {
            MappingConfig mappingConfig = yaml.loadAs(in, MappingConfig.class);
            this.ontologyConfigs = mappingConfig.getDatabases().get("yourDatabaseName").getOntology();
            ontologyConfigs.forEach(config -> logger.info("Loaded config: {}", config));
        }
    }

    private String constructUrl(String query, MappingConfig.OntologyConfig config) {
        String url = config.getUrl();
        String apiKey = config.getApiKey();
        return apiKey.isEmpty() ? String.format(url, query) : String.format(url, query, apiKey);
    }

    @PostConstruct
    public void loadDbSchemaConfigs() throws IOException {
        Yaml yaml = new Yaml(new Constructor(MappingConfig.class));
        try (InputStream in = dbSchemaConfigResource.getInputStream()) {
            MappingConfig mappingConfig = yaml.loadAs(in, MappingConfig.class);

            Map<String, MappingConfig.DatabaseConfig> databases = mappingConfig.getDatabases();
            Map<String, Object> responseStructure = (Map<String, Object>) mappingConfig.getResponseStructure();
        } catch (Exception e) {
            logger.error("Error loading database schema configuration", e);
            throw e;
        }
    }


    public List<Map<String, Object>> filterResultsByFacets(List<Map<String, Object>> results, Map<String, String> selectedFacets) {
        return results.stream()
                .filter(result -> selectedFacets.entrySet().stream()
                        .allMatch(facet -> result.containsKey(facet.getKey()) && result.get(facet.getKey()).equals(facet.getValue())))
                .collect(Collectors.toList());
    }

    @Async
    public CompletableFuture<Object> performDynFederatedSearch(
            String query, String database, String format, boolean transformToDatabaseSchema, String targetSchema) {

        Stream<MappingConfig.OntologyConfig> configsStream = ontologyConfigs.stream();
        if (database != null && !database.isEmpty()) {
            configsStream = configsStream.filter(config -> database.equalsIgnoreCase(config.getDatabase()));
        }

        List<CompletableFuture<List<Map<String, Object>>>> futures = configsStream
                .map(config -> search(query, config, format)) // Pass MappingConfig.OntologyConfig here
                .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    List<Map<String, Object>> combinedResults = futures.stream()
                            .flatMap(resultFuture -> resultFuture.join().stream())
                            .collect(Collectors.toList());

                    if (transformToDatabaseSchema && targetSchema != null && !targetSchema.isEmpty()) {
                        // Check if target schema exists in the dbSchemaConfig
                        if (dbSchemaConfig.containsKey(targetSchema)) {
                            // Retrieve the target schema's name from dbSchemaConfig
                            Map<String, Object> targetSchemaConfig = (Map<String, Object>) dbSchemaConfig.get(targetSchema);
                            // Use DynDatabaseTransform to transform the response
                            return dynDatabaseTransform.transformDatabaseResponse(
                                    targetSchema,
                                    combinedResults,
                                    targetSchemaConfig
                            );
                        } else {
                            logger.error("Target schema not found in dbSchemaConfig: {}", targetSchema);
                            throw new IllegalArgumentException("Target schema not found in dbSchemaConfig: " + targetSchema);
                        }
                    } else {
                        return combinedResults;
                    }
                });
    }


    private List<Map<String, Object>> convertToJsonLd(List<Map<String, Object>> response, MappingConfig.OntologyConfig config) {
        Map<String, Object> context = new HashMap<>();
        context.put("@vocab", "http://base4nfdi.de/ts4nfdi/schema/");
        context.put("ts", "http://base4nfdi.de/ts4nfdi/schema/");
        String type = "ts:Resource";
    
        MappingConfig.ResponseMapping responseMapping = config.getResponseMapping();

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

    private CompletableFuture<List<Map<String, Object>>> search(String query, MappingConfig.OntologyConfig config, String format) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = constructUrl(query, config);
                logger.info("Accessing URL: {}", url);
                ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    List<Map<String, Object>> transformedResponse = dynTransformResponse.dynTransformResponse(response.getBody(), config);

                    if ("jsonld".equalsIgnoreCase(format)) {
                        transformedResponse = convertToJsonLd(transformedResponse, config);
                    }

                    return transformedResponse;
                } else {
                    return List.of();
                }
            } catch (Exception e) {
                logger.error("An error occurred while processing the request", e);
                throw new RuntimeException("Error during search", e);
            }
        });
    }

}