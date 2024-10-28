package org.semantics.apigateway.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.apache.http.HttpStatus;
import org.semantics.apigateway.model.ResponseFormat;
import org.semantics.apigateway.model.SemanticArtefact;
import org.semantics.apigateway.model.TargetDbSchema;
import org.semantics.apigateway.service.ArtefactsService;
import org.semantics.apigateway.service.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;


@RestController
@RequestMapping("/")
public class ArtefactsController {
    private final ArtefactsService artefactsService;

    @Autowired
    public ArtefactsController(ArtefactsService artefactsService) {
        this.artefactsService = artefactsService;
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
    public CompletableFuture<ResponseEntity<?>> getArtefacts(
            @RequestParam(required = false) String database,
            @RequestParam(required = false) ResponseFormat format,
            @Parameter(description = "Transform the response result to a specific schema")
            @RequestParam(required = false) TargetDbSchema targetDbSchema,
            @Parameter(description = "Display more details about the request results")
            @RequestParam(required = false, defaultValue = "false") boolean showResponseConfiguration
    ) {


        return this.artefactsService.getArtefacts(database, format, targetDbSchema, showResponseConfiguration)
                .<ResponseEntity<?>>thenApply(ResponseEntity::ok)
                .exceptionally(e -> ResponseEntity.status(HttpStatus.SC_BAD_REQUEST).body("Error: " + e.getCause().getMessage()));
    }

    @CrossOrigin
    @GetMapping("/artefacts/{id}")
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
    public CompletableFuture<ResponseEntity<?>> getArtefact(
            @PathVariable String id,
            @RequestParam String uri,
            @RequestParam(required = false) String database,
            @RequestParam(required = false) ResponseFormat format,
            @Parameter(description = "Transform the response result to a specific schema")
            @RequestParam(required = false) TargetDbSchema targetDbSchema,
            @Parameter(description = "Display more details about the request results")
            @RequestParam(required = false, defaultValue = "false") boolean showResponseConfiguration
    ) {
        return this.artefactsService.getArtefactTerm(id, uri, format, targetDbSchema, showResponseConfiguration)
                .<ResponseEntity<?>>thenApply(ResponseEntity::ok)
                .exceptionally(e -> ResponseEntity.status(HttpStatus.SC_BAD_REQUEST).body("Error: " + e.getCause().getMessage()));
    }
}
