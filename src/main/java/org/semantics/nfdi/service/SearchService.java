package org.semantics.nfdi.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SearchService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Async
    public CompletableFuture<List<Object>> searchBioportal(String query, String apiKey) {
        String url = String.format("http://data.bioontology.org/search?q=%s&apikey=%s", query, apiKey);
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            List<Object> collection = (List<Object>) response.getBody().get("collection");
            return CompletableFuture.completedFuture(collection != null ? collection : List.of());
        } else {
            return CompletableFuture.completedFuture(List.of());
        }
    }

    @Async
    public CompletableFuture<List<Object>> searchOls(String query) {
        String url = String.format("https://www.ebi.ac.uk/ols/api/search?q=%s", query);
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Map responseWrapper = (Map) response.getBody().get("response");
            List<Object> docs = (List<Object>) responseWrapper.get("docs");
            return CompletableFuture.completedFuture(docs != null ? docs : List.of());
        } else {
            // Log error or handle it accordingly
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

    // Other methods for handling faceted search, autocomplete, and translation of queries across different portal configurations can be added here.

}
