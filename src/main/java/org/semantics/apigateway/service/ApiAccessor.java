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

import java.net.URI;
import java.net.URL;
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
        factory.setConnectTimeout(60000);
        factory.setReadTimeout(60000);
        this.restTemplate = new RestTemplate(factory);
        this.urls = new HashMap<>();
    }

    @Async
    public CompletableFuture<Map<String, ApiResponse>> get() {
        return get("");
    }

    @Async
    public CompletableFuture<Map<String, ApiResponse>> get(String... query) {
        ForkJoinPool customThreadPool = new ForkJoinPool(Math.max(this.urls.size(), 1));

        List<CompletableFuture<Map.Entry<String, ApiResponse>>> futures = this.urls.entrySet().stream()
                .map(config -> CompletableFuture.supplyAsync(() -> call(config.getKey(), config.getValue(), query), customThreadPool)
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


    protected ApiResponse call(String url, String apikey, String... query) {
        ApiResponse result = new ApiResponse();
        result.setUrl(url);
        String fullUrl = url;

        try {
            fullUrl = constructUrl(url, apikey, query);
            logger.info("Accessing URL: {}", fullUrl);

            long startTime = System.currentTimeMillis();

            ResponseEntity<?> response;
            URL uri = new URL(fullUrl);

            response = restTemplate.getForEntity(uri.toString(), Object.class);

            long endTime = System.currentTimeMillis();
            long responseTime = endTime - startTime;
            logger.info("URL accessed {} in {}s", fullUrl, responseTime);
            result.setResponseTime(responseTime);

            result.setStatusCode(response != null ? response.getStatusCodeValue() : 400);

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
                logger.error("API {} Response Error: Status Code - {}", fullUrl, response.getStatusCode());
                return result;
            }
        } catch (Exception e) {
            logger.error("An error occurred while processing the request {}: {}", fullUrl, e.getMessage());
            return result;
        }
    }

    private String constructUrl(String url, String apikey, String... query) {
        List<String> queries = new ArrayList<>(List.of(query));

        queries = queries.stream().filter(x -> !x.isEmpty()).collect(Collectors.toList());

        if (!apikey.isEmpty()) {
            queries.add(apikey);
        }

        if (queries.isEmpty()){
            return url;
        }else {
            return String.format(url, queries.toArray());
        }

    }
}
