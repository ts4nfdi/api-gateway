package org.semantics.nfdi.controller;

import org.semantics.nfdi.service.DynSearchService;
import org.semantics.nfdi.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
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
    public CompletableFuture<Map<String, List<Map<String, Object>>>> performDynFederatedSearch(@RequestParam String query, 
                                                                                            @RequestParam(required = false) String database) {
        return dynSearchService.performDynFederatedSearch(query, database);
    }
}
