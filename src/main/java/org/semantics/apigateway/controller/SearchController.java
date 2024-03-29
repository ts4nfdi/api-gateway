package org.semantics.apigateway.controller;

import org.apache.http.HttpStatus;
import org.semantics.apigateway.service.DynSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;


@RestController
@RequestMapping("/api-gateway")
public class SearchController {

    private final DynSearchService dynSearchService;

    @Autowired
    public SearchController(DynSearchService dynSearchService) {
        this.dynSearchService = dynSearchService;
    }

    @GetMapping("/federatedSearch")
    public CompletableFuture<ResponseEntity<?>> performDynFederatedSearch(@RequestParam String query,
                                                                          @RequestParam(required = false) String database,
                                                                          @RequestParam(required = false) String format,
                                                                          @RequestParam(required = false) String targetDbSchema) throws IOException {
        return dynSearchService.performDynFederatedSearch(query, database, format, targetDbSchema)
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
