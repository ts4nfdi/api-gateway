package org.semantics.nfdi.controller;

import org.semantics.nfdi.service.SearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.concurrent.CompletableFuture;


@RestController
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/search")
    public CompletableFuture<List<Object>> search(@RequestParam String query, @RequestParam(required = false) String bioportalApiKey) {
        return searchService.performFederatedSearch(query, bioportalApiKey);
    }

}
