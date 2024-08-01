package org.semantics.apigateway.utils;

import lombok.AllArgsConstructor;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import org.semantics.apigateway.config.OntologyConfig;
import org.semantics.apigateway.model.JsonLdTransform;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@AllArgsConstructor
public class ApiAccessor {

    private final RestTemplate restTemplate = new RestTemplate();
    private Map<String, String> urls;
    private Logger logger;


    public CompletableFuture<Map<String, Map<String, Object>>> get(String query) {
        List<CompletableFuture<Map.Entry<String, Map<String, Object>>>> futures = this.urls.entrySet().stream()
                .map(config -> {
                    CompletableFuture<Map<String, Object>> future = call(query, config.getKey(), config.getValue());
                    return future.thenApply(x -> Map.entry(config.getKey(), x));
                })
                .collect(Collectors.toList());


        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    try {

                        Map<String, Map<String, Object>> combinedResults = new HashMap<>();


                        futures.stream().forEach(future -> {
                            Map.Entry<String, Map<String, Object>> a = future.join();
                            combinedResults.put(a.getKey(), a.getValue());
                        });
                        
                        logger.info("Combined results before transformation: {}", combinedResults);

                        return combinedResults;
                    } catch (Exception e) {
                        logger.error("Error in transforming results for database schema: {}", e.getMessage(), e);
                        //future.completeExceptionally(e);
                        return null;
                    }
                });
    }

    /**
     * Asynchronously performs a search query against an API url
     */
    @Async
    protected CompletableFuture<Map<String, Object>> call(String query, String url, String apikey) {
        Logger logger = this.logger;
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        try {
            url = constructUrl(query, url, apikey);
            logger.info("Accessing URL: {}", url);

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                logger.info("Raw API Response: {}", response.getBody());

                future.complete(response.getBody());

//                List<Map<String, Object>> transformedResponse = dynTransformResponse.dynTransformResponse(response.getBody(), config);
//
//                if ("jsonld".equalsIgnoreCase(format)) {
//                    transformedResponse = JsonLdTransform.convertToJsonLd(transformedResponse, config);
//                }
//
//                logger.info("Transformed API Response: {}", transformedResponse);
//
            } else {
                logger.error("API Response Error: Status Code - {}", response.getStatusCode());
                future.complete(Collections.emptyMap());
            }
        } catch (Exception e) {
            logger.error("An error occurred while processing the request: {}", e.getMessage(), e);
            future.complete(Collections.emptyMap());
//          future.completeExceptionally(e);
        }

        return future;
    }

    private String constructUrl(String query, String url, String apikey) {
        return apikey.isEmpty() ? String.format(url, query) : String.format(url, query, apikey);
    }

}
