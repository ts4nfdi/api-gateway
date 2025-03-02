package org.semantics.apigateway.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.apache.http.HttpStatus;
import org.semantics.apigateway.model.ResponseFormat;
import org.semantics.apigateway.model.SemanticArtefact;
import org.semantics.apigateway.model.TargetDbSchema;
import org.semantics.apigateway.model.user.User;
import org.semantics.apigateway.service.ArtefactsService;
import org.semantics.apigateway.service.auth.AuthService;
import org.semantics.apigateway.service.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


@RestController
@RequestMapping("/")
public class ArtefactsController {
    private final ArtefactsService artefactsService;
    private final AuthService authService;

    public ArtefactsController(ArtefactsService artefactsService, AuthService authService) {
        this.artefactsService = artefactsService;
        this.authService = authService;
    }


    @CrossOrigin
    @GetMapping("/artefacts")
    @Operation(
            summary = "Get information about all semantic artefacts.",
            description = "Retrieves a collection of all semantic artefacts.",
            operationId = "getArtefacts",
            tags = {"Artefacts"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of artefacts",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SemanticArtefact.class))),
            @ApiResponse(responseCode = "404", description =  "Artefacts not found",
                    content = @Content(mediaType = "application/json"))
    })
    @SecurityRequirement(name = "BearerAuth")
    public ResponseEntity<?> getArtefacts(
            @RequestParam(required = false) String database,
            @RequestParam(required = false) ResponseFormat format,
            @Parameter(description = "Transform the response result to a specific schema")
            @RequestParam(required = false) TargetDbSchema targetDbSchema,
            @Parameter(description = "Display more details about the request results")
            @RequestParam(required = false, defaultValue = "false") boolean showResponseConfiguration,
            @Parameter(description = "Collection id to browse terminologies in")
            @RequestParam(required = false) String collectionId
    ) throws ExecutionException, InterruptedException {
        User user = authService.tryGetCurrentUser();
        return this.artefactsService.getArtefacts(database, format, targetDbSchema, showResponseConfiguration, collectionId, user)
                .<ResponseEntity<?>>thenApply(ResponseEntity::ok).get();
    }

    @CrossOrigin
    @GetMapping("/artefacts/{id}")
    @Operation(
            summary = "Get information about a semantic artefact.",
            description = "Retrieves information about a specific semantic artefact.",
            operationId = "getArtefact",
            tags = {"Artefacts"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of the artefact",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SemanticArtefact.class))),
            @ApiResponse(responseCode = "404", description = "Artefact not found",
                    content = @Content(mediaType = "application/json"))
    })
    public CompletableFuture<ResponseEntity<?>> getArtefact(
            @PathVariable("id") String id,
            @RequestParam(required = false) String database,
            @RequestParam(required = false) ResponseFormat format,
            @Parameter(description = "Transform the response result to a specific schema")
            @RequestParam(required = false) TargetDbSchema targetDbSchema,
            @Parameter(description = "Display more details about the request results")
            @RequestParam(required = false, defaultValue = "false") boolean showResponseConfiguration
    ) {
        return this.artefactsService.getArtefact(id, format, targetDbSchema, showResponseConfiguration)
                .<ResponseEntity<?>>thenApply(ResponseEntity::ok)
                .exceptionally(e -> ResponseEntity.status(HttpStatus.SC_BAD_REQUEST).body("Error: " + e.getCause().getMessage()));
    }


    @CrossOrigin
    @GetMapping("/artefacts/{id}/terms")
    @Operation(
            summary = "Get a list of all owl:Classes or skos:Concepts within an artefact.",
            description = "Retrieves a list of all the owl:Classes within a specific artefact.",
            operationId = "getArtefactClasses",
            tags = {"Artefacts"}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of all owl:Class or skos:Concept terms",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = SemanticArtefact.class))),
            @ApiResponse(responseCode = "404", description = "Artefact not found",
                    content = @Content(mediaType = "application/json"))
    })
    public CompletableFuture<ResponseEntity<?>> getArtefactTerms(
            @PathVariable String id,
            @RequestParam String uri,
            @RequestParam(required = false) String database,
            @RequestParam(required = false) ResponseFormat format,
            @Parameter(description = "Transform the response result to a specific schema")
            @RequestParam(required = false) TargetDbSchema targetDbSchema,
            @Parameter(description = "Display more details about the request results")
            @RequestParam(required = false, defaultValue = "false") boolean showResponseConfiguration
    ) {
        return this.artefactsService.getArtefactTerm(id, uri, database, format, targetDbSchema, showResponseConfiguration)
                .<ResponseEntity<?>>thenApply(ResponseEntity::ok)
                .exceptionally(e -> ResponseEntity.status(HttpStatus.SC_BAD_REQUEST).body("Error: " + e.getCause().getMessage()));
    }
}
