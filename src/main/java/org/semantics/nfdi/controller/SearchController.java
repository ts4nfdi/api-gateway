package org.semantics.nfdi.controller;

import org.apache.http.HttpStatus;
import org.semantics.nfdi.service.DynSearchService;
import org.semantics.nfdi.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;


@RestController
@RequestMapping("/nfdi")
public class SearchController {

    private final SearchService searchService;
    private final DynSearchService dynSearchService;

    @Autowired
    public SearchController(SearchService searchService, DynSearchService dynSearchService) {
        this.searchService = searchService;
        this.dynSearchService = dynSearchService;
    }

    @GetMapping("/search")
    public CompletableFuture<List<Object>> search(@RequestParam String query, @RequestParam(required = false) String ApiKey) {
        return searchService.performFederatedSearch(query, ApiKey);
    }

    @GetMapping("/federatedSearch")
    public CompletableFuture<ResponseEntity<?>> performDynFederatedSearch(@RequestParam String query, 
                                                                        @RequestParam(required = false) String database) {
        return dynSearchService.performDynFederatedSearch(query)
                .<ResponseEntity<?>>thenApply(ResponseEntity::ok)
                .exceptionally(e -> {
                    if (e.getCause() instanceof IllegalArgumentException) {
                        return ResponseEntity
                                .status(HttpStatus.SC_BAD_REQUEST)
                                .body("Error: " + e.getCause().getMessage());
                    }
                    return ResponseEntity
                            .status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                            .body("Error: An internal server error occurred");
                });
    }
}
