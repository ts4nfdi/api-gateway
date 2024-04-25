package org.semantics.apigateway.controller;

import org.apache.http.HttpStatus;
import org.apache.jena.rdf.model.ResourceFactory;
import org.semantics.apigateway.service.DynSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;


@RestController
@RequestMapping("/api-gateway")
public class GatewayController {

    private final DynSearchService dynSearchService;

    @Autowired
    public GatewayController(DynSearchService dynSearchService) {
        this.dynSearchService = dynSearchService;
    }

    @CrossOrigin
    @GetMapping("/federatedSearch")
    public CompletableFuture<ResponseEntity<?>> performDynFederatedSearch(@RequestParam String query,
                                                                          @RequestParam(required = false) String database,
                                                                          @RequestParam(required = false) String format,
                                                                          @RequestParam(required = false) String targetDbSchema) {
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

    @CrossOrigin
    @GetMapping("/ols/api/select")
    public CompletableFuture<ResponseEntity<?>>
    performDynFederatedSearchInOLSTargetDBSchema(@RequestParam Map<String,String> allParams) {

        String query;
        if (allParams.containsKey("q") || allParams.containsKey("query")){
            if (allParams.containsKey("q")) {
                query = allParams.get("q");
            } else {
                query = allParams.get("query");
            }
        } else {
            query = "*";
        }

        return dynSearchService.performDynFederatedSearch(query + "*", allParams.get("database"), allParams.get("format"), "ols")
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

    @CrossOrigin
    @GetMapping("/ols/api/ontologies/{ontology}/terms")
    public CompletableFuture<ResponseEntity<?>>
    getTermsInOLSTargetDBSchema(@PathVariable("ontology") String terminologyName, @RequestParam Map<String,String> allParams) {

        CompletableFuture<Object> future = new CompletableFuture<>();
        Map<String, Object> data = new HashMap<>();
        data.put("short_form", ResourceFactory.createResource(String.valueOf(allParams.get("iri"))).getLocalName().toLowerCase());
        data.put("ontology_name", terminologyName);
        Map<String, List<Map>> terms = new HashMap<>();
        List<Map> combinedData = new ArrayList<>();
        combinedData.add(data);
        terms.put("terms", combinedData);
        Map<String, Object> response = new HashMap<>();
        response.put("_embedded", terms);

        future.complete(response);
        return future.<ResponseEntity<?>>thenApply(ResponseEntity::ok)
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
