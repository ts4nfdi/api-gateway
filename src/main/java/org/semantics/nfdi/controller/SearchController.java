package org.semantics.nfdi.controller;

import org.apache.http.HttpStatus;
import org.semantics.nfdi.service.DynSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/nfdi")
public class SearchController {

    private final DynSearchService dynSearchService;

    @Autowired
    public SearchController(DynSearchService dynSearchService) {
        this.dynSearchService = dynSearchService;
    }

    @GetMapping("/federatedSearch")
    public CompletableFuture<ResponseEntity<?>> performDynFederatedSearch(
            @RequestParam String query,
            @RequestParam(required = false) String database,
            @RequestParam(required = false) String format,
            @RequestParam(required = false) String targetSchema) {

        boolean transformToDatabaseSchema = targetSchema != null && !targetSchema.isEmpty();

        return dynSearchService.performDynFederatedSearch(query, database, format, transformToDatabaseSchema)
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
