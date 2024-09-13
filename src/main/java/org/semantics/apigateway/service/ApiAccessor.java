package org.semantics.apigateway.service;

import lombok.AllArgsConstructor;
import lombok.Setter;
import org.semantics.apigateway.model.responses.ApiResponse;
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

    @Async
    public CompletableFuture<Map<String, ApiResponse>> get() {
        return get("");
    }

    @Async
    public CompletableFuture<Map<String, ApiResponse>> get(String query) {
        ForkJoinPool customThreadPool = new ForkJoinPool(10);

        List<CompletableFuture<Map.Entry<String, ApiResponse>>> futures = this.urls.entrySet().stream()
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
                    return Collections.emptyMap();
                });
    }


    protected ApiResponse call(String query, String url, String apikey) {
        ApiResponse result = new ApiResponse();
        result.setUrl(url);

        try {
            String fullUrl = constructUrl(query, url, apikey);
            logger.info("Accessing URL: {}", fullUrl);

            long startTime = System.currentTimeMillis();

            ResponseEntity<?> response;
            response = restTemplate.getForEntity(fullUrl, Object.class);

            long endTime = System.currentTimeMillis();
            long responseTime = endTime - startTime;

            result.setResponseTime(responseTime);

            result.setStatusCode(response.getStatusCodeValue());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                logger.debug("Raw API Response: {}", response.getBody());
                if (response.getBody() instanceof List) {
                    Map<String, Object> out = new HashMap<>();
                    out.put("collection", response.getBody());
                    result.setResponseBody(out);
                } else {
                    result.setResponseBody((Map<String, Object>) response.getBody());
                }
                return result;
            } else {
                logger.error("API {} Response Error: Status Code - {}", url,response.getStatusCode());
                return result;
            }
        } catch (Exception e) {
            logger.error("An error occurred while processing the request {}: {}", url, e.getMessage(), e);
            return result;
        }
    }

    private String constructUrl(String query, String url, String apikey) {
        if (query.isEmpty()) {
            return apikey.isEmpty() ? url : String.format(url, apikey);
        } else {
            return apikey.isEmpty() ? String.format(url, query) : String.format(url, query, apikey);
        }
    }
}
