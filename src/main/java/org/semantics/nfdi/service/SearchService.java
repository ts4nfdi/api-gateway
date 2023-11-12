package org.semantics.nfdi.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.semantics.nfdi.model.TransformResponse;

@Service
public class SearchService {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);

    private final TransformResponse transformResponse = new TransformResponse();

    @Async
    public CompletableFuture<List<Object>> searchBioportal(String query, String apiKey) {
        String url = String.format("https://data.biodivportal.gfbio.dev/search?q=%s&apikey=%s", query, apiKey);
        logger.info("Accessing URL: {}", url);
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            // logger.info("Received response from Bioportal: {}", response.getBody());
            return CompletableFuture.completedFuture(transformResponse.transformResponse(response.getBody(), "Bioportal"));
        } else {
            logger.error("Error accessing URL: {}", url);
            return CompletableFuture.completedFuture(List.of());
        }
    }

    @Async
    public CompletableFuture<List<Object>> searchOls(String query) {
        String url = String.format("https://www.ebi.ac.uk/ols/api/search?q=%s", query);
        logger.info("Accessing URL: {}", url);
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            // logger.info("Received response from OLS: {}", response.getBody());
            return CompletableFuture.completedFuture(transformResponse.transformResponse(response.getBody(), "OLS"));
        } else {
            logger.error("Error accessing URL: {}", url);
            return CompletableFuture.completedFuture(List.of());
        }
    }

    @Async
    public CompletableFuture<List<Object>> searchTerminologies(String query) {
        String url = String.format("https://terminologies.gfbio.org/api/terminologies/search?query=%s", query);
        logger.info("Accessing URL: {}", url);
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            // logger.info("Received response from Terminologies: {}", response.getBody());
            return CompletableFuture.completedFuture(transformResponse.transformResponse(response.getBody(), "Terminologies"));
        } else {
            logger.error("Error accessing URL: {}", url);
            return CompletableFuture.completedFuture(List.of());
        }
    }
    

    public CompletableFuture<List<Object>> performFederatedSearch(String query, String bioportalApiKey) {
        CompletableFuture<List<Object>> bioPortalFuture = searchBioportal(query, bioportalApiKey);
        CompletableFuture<List<Object>> olsFuture = searchOls(query);

        return CompletableFuture.allOf(bioPortalFuture, olsFuture).thenApply(v -> {
            List<Object> combinedResults = Stream.concat(bioPortalFuture.join().stream(), olsFuture.join().stream())
                                                 .collect(Collectors.toList());
            return combinedResults;
        });
    }

}
