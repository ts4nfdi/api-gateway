package org.semantics.apigateway.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.apache.lucene.queryparser.classic.ParseException;
import org.semantics.apigateway.model.*;
import org.semantics.apigateway.model.responses.ErrorResponse;
import org.semantics.apigateway.model.user.User;
import org.semantics.apigateway.service.auth.AuthService;
import org.semantics.apigateway.service.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


@RestController
@RequestMapping("/")
public class SearchController {
    private final SearchService searchService;
    private final AuthService authService;

    public SearchController(SearchService searchService, AuthService authService) {
        this.searchService = searchService;
        this.authService = authService;
    }


    @Operation(summary = "Search all of the content in a catalogue.", description = "The returned data should include a description of the type of data that is being returned. For example the returned content could be SKOS Concepts or OWL Classes.", tags = {"Search"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Successful retrieval of terms",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SemanticArtefact.class))),
            @ApiResponse(responseCode = "404", description = "")})
    @CrossOrigin
    @GetMapping("/search")
    @SecurityRequirement(name = "BearerAuth")
    public ResponseEntity<?> performDynFederatedSearch(
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
            @RequestParam(required = false, defaultValue = "false") boolean showResponseConfiguration,
            @Parameter(description = "The terminologies to search in (e.g agrovoc,cgo)")
            @RequestParam(required = false) String[] terminologies,
            @Parameter(description = "Collection id to search in")
            @RequestParam(required = false) String collectionId
    ) throws ExecutionException, InterruptedException {
        User user = authService.tryGetCurrentUser();
        return searchService.performSearch(query, database, format, targetDbSchema, showResponseConfiguration, terminologies, collectionId, user)
                .thenApply(ResponseEntity::ok).get();
    }
}
