package org.semantics.apigateway.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.apache.http.HttpStatus;
import org.apache.jena.rdf.model.ResourceFactory;
import org.semantics.apigateway.service.DynSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;


@RestController
@RequestMapping("/")
public class GatewayController {

    private final DynSearchService dynSearchService;

    @Autowired
    public GatewayController(DynSearchService dynSearchService) {
        this.dynSearchService = dynSearchService;
    }

    @CrossOrigin
    @GetMapping("/")
    public void home(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/swagger-ui/index.html?configUrl=/api-gateway/openapi/swagger-config");
    }


    @CrossOrigin
    @Operation(summary = "Search all of the content in a catalogue.", description = "The returned data should include a description of the type of data that is being returned. For example the returned content could be SKOS Concepts or OWL Classes.", tags = {"Search"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = ""),

            @ApiResponse(responseCode = "404", description = "")})
    @GetMapping("/search")
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
    @Operation(summary = "", description = "", tags = {"OLS"})
    @GetMapping("/ols/api/select")
    public CompletableFuture<ResponseEntity<?>>
    performDynFederatedSearchInOLSTargetDBSchema(@RequestParam Map<String, String> allParams) {

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
    @Operation(summary = "", description = "", tags = {"OLS"})
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
