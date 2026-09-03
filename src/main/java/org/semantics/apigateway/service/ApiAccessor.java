package org.semantics.apigateway.service;

import lombok.AllArgsConstructor;
import lombok.Setter;
import org.semantics.apigateway.config.EndpointParameterMapping;
import org.semantics.apigateway.model.responses.ApiResponse;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Setter
@AllArgsConstructor
@Service
public class ApiAccessor {

    private RestTemplate restTemplate;
    private Map<String, UrlConfig> urls;
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
    public CompletableFuture<Map<String, ApiResponse>> get(long timeoutMillis) {
        return get(timeoutMillis, new HashMap<>());
    }

    @Async
    public CompletableFuture<Map<String, ApiResponse>> get(long timeoutMillis, Map<RequestParameter, String> queryParams) {
        ForkJoinPool customThreadPool = new ForkJoinPool(Math.max(this.urls.size(), 1));

        List<CompletableFuture<Map.Entry<String, ApiResponse>>> futures = this.urls.entrySet().stream()
                .map(config -> CompletableFuture.supplyAsync(() -> call(config.getKey(), config.getValue(), queryParams), customThreadPool)
                        .thenApply(response -> Map.entry(config.getKey(), response))
                )
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .collect(Collectors.toMap(
                                future -> future.join().getKey(),
                                future -> future.join().getValue()
                        ))
                ).completeOnTimeout(Map.of(), timeoutMillis, TimeUnit.MILLISECONDS)
                .exceptionally(e -> {
                    logger.error("Error processing results: {}", e.getMessage(), e);
                    return Collections.emptyMap();
                });
    }

    public ApiResponse call(String url, UrlConfig urlConfig, Map<RequestParameter, String> queryParams) {
        ApiResponse result = new ApiResponse();
        String fullUrl = url;
        result.setUrl(url);
        
        List<RequestParameter> unsupportedParams = checkForUnsupportedParams(queryParams.keySet(), urlConfig.parameterMappings().keySet());
        result.setUnsupportedParams(unsupportedParams);
        if (!unsupportedParams.isEmpty()) {
           return result;
        }
        
        try {
            fullUrl = constructUrl(url, urlConfig, queryParams);

            if (cacheService.exists(fullUrl) && cacheEnabled) {
                logger.info("Cached result for request URL: {} and query parameters: {}", url, queryParams);
                return (ApiResponse) cacheService.read(fullUrl);
            }

            if (!cacheEnabled)
                logger.info("Cache is disabled");

            if (url.endsWith("/localData")) {
                logger.info("Local data requested for URL: {}", fullUrl);
                Map<String, Object> out = new HashMap<>();
                List<Object> collection = new ArrayList<>();
                collection.add(new HashMap<>());
                out.put("collection", collection);
                result.setResponseBody(out);
                return result;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.ALL));
            if (url.startsWith("https://test.iconclass.org/")) {
                headers.setBearerAuth("foobarbax");
            }
            HttpEntity<String> entity = new HttpEntity<>("body", headers);
            
            logger.info("Accessing URL: {}", fullUrl);
            
            long startTime = System.currentTimeMillis();

            ResponseEntity<?> response;
            URL uri = new URL(fullUrl);
            if (unDecodeUrl) {
                restTemplate.setInterceptors(Collections.singletonList(new UriDecodingInterceptor()));
            }
            
            response = restTemplate.exchange(uri.toString(), HttpMethod.GET, entity, Object.class);

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
                    Map<String, Object> resultMap = (Map<String, Object>) response.getBody();
                    if (resultMap.containsKey("error")) {
                        logger.error("Backend returned an error message processing the request {}: {}", fullUrl, resultMap.get("error"));
                    } else {
                        result.setResponseBody(resultMap);
                    }
                    return result;
                }
                logger.info("Write cache for request URL: {} and query parameters: {}", url, queryParams);
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
    
    private List<RequestParameter> checkForUnsupportedParams(Set<RequestParameter> requiredParameters, Set<RequestParameter> availableParameters) {
        return requiredParameters.stream().filter(param ->
                param.getType() == RequestParameter.Type.backendSpecific &&
                !availableParameters.contains(param)).collect(Collectors.toList());
    }
    
    private String constructUrl(String url, UrlConfig config, Map<RequestParameter, String> requestParameters) {
        String apikey = config.apikey();
        
        boolean isCaseInsensitive = config.caseInSensitive();
        
        if(isCaseInsensitive && requestParameters.get(RequestParameter.artefact) != null)
            requestParameters.put(RequestParameter.artefact, (requestParameters.get(RequestParameter.artefact)).toUpperCase());
        
        try {
            requestParameters.put(RequestParameter.page, "" + (Integer.parseInt(Optional.ofNullable(requestParameters.get(RequestParameter.page)).orElse("0")) + config.pagination().getFirst()));
        } catch (NumberFormatException e) {
            logger.info("Pagination parameter missing for URL {} with query: {}", url, requestParameters);
        }
        
        if (!apikey.isEmpty()) {
            requestParameters.put(RequestParameter.apiKey, apikey);
        }

        if (requestParameters.isEmpty()) {
            return url;
        } else {
            return formatUrl(url, config, requestParameters);
        }
    }
    
    private final static Pattern pathParamPattern = Pattern.compile("\\{(.*?)}");
    
    private String formatUrl(String urlTemplate, UrlConfig config, Map<RequestParameter, String> queryParams) {
        String url = pathParamPattern.matcher(urlTemplate).replaceAll(match -> {
            String paramKey = match.group(1);
            String paramValue = queryParams.get(RequestParameter.valueOf(paramKey));
            if (paramValue == null) {
                logger.error("No value for path parameter '{}' for url {}", paramKey, urlTemplate);
                return "";
            }
            return paramValue;
        });
        
        for (Map.Entry<RequestParameter, EndpointParameterMapping> entry : config.parameterMappings().entrySet()) {
            RequestParameter key = entry.getKey();
            EndpointParameterMapping mapping = entry.getValue();
            String paramValue = queryParams.get(key);
            if (paramValue != null) {
                url = appendQueryParam(url, mapping.serializeParameter(paramValue));
            } else if (!mapping.isOptional()) {
                logger.error("No value for query parameter '{}' for url {}", key, urlTemplate);
            }
        }
        
        return url;
    }
    
    private String appendQueryParam(String url, String param) {
        if (url.contains("?")) {
            url = url + "&" + param;
        } else  {
            url = url + "?" + param;
        }
        return url;
    }
    
}
