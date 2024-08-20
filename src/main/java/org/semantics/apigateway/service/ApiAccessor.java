package org.semantics.apigateway.service;

import lombok.AllArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

@Setter
@AllArgsConstructor
@Service
public class ApiAccessor {

    private RestTemplate restTemplate;
    private Map<String, String> urls;
    private Logger logger;


    public ApiAccessor() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);
        this.restTemplate = new RestTemplate(factory);
        this.urls = new HashMap<>();
    }

    public CompletableFuture<Map<String, Map<String, Object>>> get(String query) {
        ForkJoinPool customThreadPool = new ForkJoinPool(10);

        List<CompletableFuture<Map.Entry<String, Map<String, Object>>>> futures = this.urls.entrySet().stream()
                .map(config -> CompletableFuture.supplyAsync(() -> call(query, config.getKey(), config.getValue()), customThreadPool)
                        .thenApply(response -> Map.entry(config.getKey(), response))
                )
                .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .collect(Collectors.toMap(
                                future -> future.join().getKey(),
                                future -> future.join().getValue()
                        ))
                )
                .exceptionally(e -> {
                    logger.error("Error processing results: {}", e.getMessage(), e);
                    return Collections.emptyMap();  // Return an empty map on error
                });
    }

    @Async
    protected Map<String, Object> call(String query, String url, String apikey) {
        try {
            String fullUrl = constructUrl(query, url, apikey);
            logger.info("Accessing URL: {}", fullUrl);

            ResponseEntity<Map> response = restTemplate.getForEntity(fullUrl, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                logger.debug("Raw API Response: {}", response.getBody());
                return response.getBody();
            } else {
                logger.error("API Response Error: Status Code - {}", response.getStatusCode());
                return Collections.emptyMap();
            }
        } catch (Exception e) {
            logger.error("An error occurred while processing the request: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    private String constructUrl(String query, String url, String apikey) {
        return apikey.isEmpty() ? String.format(url, query) : String.format(url, query, apikey);
    }
}
