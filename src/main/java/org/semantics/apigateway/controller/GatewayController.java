package org.semantics.apigateway.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.apache.http.HttpStatus;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.lucene.queryparser.classic.ParseException;
import org.semantics.apigateway.service.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;


@RestController
@RequestMapping("/")
public class GatewayController {

    private final SearchService searchService;

    public GatewayController(SearchService searchService) {
        this.searchService = searchService;
    }

    @CrossOrigin
    @GetMapping("/")
    public void home(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/swagger-ui/index.html?configUrl=/api-gateway/openapi/swagger-config");
    }


    @CrossOrigin
    @Operation(summary = "", description = "", tags = {"ols"})
    @GetMapping("/ols/api/select")
    public CompletableFuture<ResponseEntity<?>>
    performDynFederatedSearchInOLSTargetDBSchema(@RequestParam Map<String, String> allParams) throws IOException, ParseException {

        String query;
        if (allParams.containsKey("q") || allParams.containsKey("query")) {
            if (allParams.containsKey("q")) {
                query = allParams.get("q");
            } else {
                query = allParams.get("query");
            }
        } else {
            query = "*";
        }

        return searchService.performSearch(query + "*", allParams.get("database"), allParams.get("format"), "ols", false)
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
    @Operation(summary = "", description = "", tags = {"ols"})
    @GetMapping("/ols/api/ontologies/{ontology}/terms")
    public CompletableFuture<ResponseEntity<?>>
    getTermsInOLSTargetDBSchema(@PathVariable("ontology") String terminologyName, @RequestParam Map<String, String> allParams) {

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
