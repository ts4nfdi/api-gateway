package org.semantics.apigateway.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.apache.http.HttpStatus;
import org.apache.lucene.queryparser.classic.ParseException;
import org.semantics.apigateway.model.Database;
import org.semantics.apigateway.model.ResponseFormat;
import org.semantics.apigateway.model.TargetDbSchema;
import org.semantics.apigateway.service.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;


@RestController
@RequestMapping("/")
public class SearchController {
    private final SearchService searchService;

    @Autowired
    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }


    @Operation(summary = "Search all of the content in a catalogue.", description = "The returned data should include a description of the type of data that is being returned. For example the returned content could be SKOS Concepts or OWL Classes.", tags = {"Search"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = ""),

            @ApiResponse(responseCode = "404", description = "")})
    @CrossOrigin
    @GetMapping("/search")
    public CompletableFuture<ResponseEntity<?>> performDynFederatedSearch(
            @Parameter(description = "The text to search", example = "plant")
            @RequestParam String query,
            @Parameter(description = "Source to run search against")
            @RequestParam(required = false) Database database,
            @RequestParam(required = false) ResponseFormat format,
            @Parameter(description = "Transform the response result to a specific schema")
            @RequestParam(required = false) TargetDbSchema targetDbSchema,
            @Parameter(description = "Display more details about the request results")
            @RequestParam(required = false, defaultValue = "false") boolean showResponseConfiguration
    ) {


        return searchService.performSearch(query, database, format, targetDbSchema, showResponseConfiguration)
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
