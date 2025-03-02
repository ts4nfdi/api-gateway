package org.semantics.apigateway.service;

import lombok.AllArgsConstructor;
import lombok.Setter;
import org.semantics.apigateway.model.responses.ApiResponse;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
    private boolean unDecodeUrl;
    private CacheService cacheService;
    private boolean cacheEnabled;

 
    @Autowired
    public ApiAccessor(CacheManager cacheManager) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        this.cacheService = new CacheService(cacheManager);
        factory.setConnectTimeout(60000);
        factory.setReadTimeout(60000);
        this.restTemplate = new RestTemplate(factory);
        this.urls = new HashMap<>();
        this.unDecodeUrl = false;
        this.cacheEnabled = true;
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



    public ApiResponse call(String url, String apikey, String... query) {



        ApiResponse result = new ApiResponse();
        String fullUrl = url;
        result.setUrl(url);

        try {
            fullUrl = constructUrl(url, apikey, query);

            if (cacheService.exists(fullUrl) && cacheEnabled) {
                logger.info("Cached result for request URL: {} and query parameters: {}", url, query);
                return (ApiResponse) cacheService.read(fullUrl);
            }

            if(!cacheEnabled)
                logger.info("Cache is disabled");

            logger.info("Accessing URL: {}", fullUrl);

            long startTime = System.currentTimeMillis();

            ResponseEntity<?> response;
            URL uri = new URL(fullUrl);
            if(unDecodeUrl){
                restTemplate.setInterceptors(Collections.singletonList(new UriDecodingInterceptor()));
            }
            response = restTemplate.getForEntity(uri.toString(), Object.class);

            long endTime = System.currentTimeMillis();
            long responseTime = endTime - startTime;
            logger.info("URL accessed {} in {}s", fullUrl, responseTime);
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
                logger.info("Write cache for request URL: {} and query parameters: {}", url, query);
                cacheService.write(fullUrl, result);
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
