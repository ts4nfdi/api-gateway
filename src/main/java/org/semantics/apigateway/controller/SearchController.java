package org.semantics.apigateway.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.apache.http.HttpStatus;
import org.apache.lucene.queryparser.classic.ParseException;
import org.semantics.apigateway.model.*;
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
    @ApiResponses(value = {         @ApiResponse(responseCode = "200", description = "Successful retrieval of terms",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SemanticArtefact.class))),
            @ApiResponse(responseCode = "404", description = "")})
    @CrossOrigin
    @GetMapping("/search")
    public CompletableFuture<ResponseEntity<?>> performDynFederatedSearch(
            @Parameter(description = "The text to search", example = "plant")
            @RequestParam String query,
            @Parameter(
                    description = "Choose on which databases of backend type to run the search",
                    schema = @Schema(type = "array",
                            example = "biodivportal,agroportal,agrovoc,ebi")
            )
            @RequestParam(required = false) String database,
            @RequestParam(required = false) ResponseFormat format,
            @Parameter(description = "Transform the response result to a specific schema")
            @RequestParam(required = false) TargetDbSchema targetDbSchema,
            @Parameter(description = "Display more details about the request results")
            @RequestParam(required = false, defaultValue = "false") boolean showResponseConfiguration
    ) {


        return searchService.performSearch(query, database, format, targetDbSchema, showResponseConfiguration)
                .<ResponseEntity<?>>thenApply(ResponseEntity::ok)
                .exceptionally(e -> ResponseEntity.status(HttpStatus.SC_BAD_REQUEST).body("Error: " + e.getCause().getMessage()));
    }
}
