package org.semantics.apigateway.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.semantics.apigateway.model.RDFResource;
import org.semantics.apigateway.model.RDFType;
import org.semantics.apigateway.model.ResponseFormat;
import org.semantics.apigateway.model.SemanticArtefact;
import org.semantics.apigateway.service.ArtefactsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/artefacts")
public class ArtefactsController {


    private final ArtefactsService artefactService;

    @Autowired
    public ArtefactsController(ArtefactsService artefactService) {
        this.artefactService = artefactService;
    }

    @Operation(summary = "Get information about all semantic artefacts.", description = "Retrieves a collection of all semantic artefacts.", tags={ "Artefact"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = ""),

            @ApiResponse(responseCode = "404", description = "") })
    @GetMapping
    public ResponseEntity<List<SemanticArtefact>> getAllArtefacts(
            @RequestParam(required = false, defaultValue = "json") ResponseFormat format,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int pagesize,
            @RequestParam(required = false, defaultValue = "all") String display) {

        return null;
    }

    @Operation(summary = "Get information about a semantic artefact.", description = "Retrieves information about a semantic artefact.", tags={ "Artefact" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = ""),

            @ApiResponse(responseCode = "404", description = "") })
    @GetMapping("/{artefactID}")
    public ResponseEntity<SemanticArtefact> getArtefact(
            @PathVariable String artefactID,
            @RequestParam(required = false, defaultValue = "json") ResponseFormat format,
            @RequestParam(required = false, defaultValue = "all") String display) {
        return ResponseEntity.ok(new SemanticArtefact());
    }


    @Operation(summary = "Get a list of all the resources within an artefact.", description = "Retrieves a list of all the resources within an artefact.", tags={ "Artefact" })
    @GetMapping("/{artefactID}/resources")
    public ResponseEntity<List<RDFResource>> getArtefactResources(
            @PathVariable String artefactID,
            @RequestParam(required = false, defaultValue = "json") ResponseFormat format,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int pagesize,
            @RequestParam(required = false) RDFType type
            ) {
        return ResponseEntity.ok(List.of());
    }



//
//    @Operation(summary = "Get a list of all owl:Classes within an artefact.", description = "Retrieves a list of all the owl:Classes within an artefact.", tags={ "Artefact" })
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = ""),
//
//            @ApiResponse(responseCode = "404", description = "") })
//    @GetMapping("/{artefactID}/resources/classes")
//    public ResponseEntity<List<RDFResource>> getArtefactClasses(
//            @PathVariable String artefactID,
//            @RequestParam(required = false, defaultValue = "json") ResponseFormat format,
//            @RequestParam(defaultValue = "1") int page,
//            @RequestParam(defaultValue = "50") int pagesize) {
//
//        return ResponseEntity.ok(List.of());
//    }
//
//
//
//    @Operation(summary = "Get a list of all skos:Concept within an artefact.", description = "Retrieves a list of all skos:Concept within an artefact.", tags={ "Artefact" })
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = ""),
//
//            @ApiResponse(responseCode = "404", description = "") })
//    @GetMapping("/{artefactID}/resources/concepts")
//    public ResponseEntity<List<RDFResource>> getArtefactConcepts(
//            @PathVariable String artefactID,
//            @RequestParam(required = false, defaultValue = "json") ResponseFormat format,
//            @RequestParam(defaultValue = "1") int page,
//            @RequestParam(defaultValue = "50") int pagesize) {
//        return ResponseEntity.ok(List.of());
//    }
//
//
//    @Operation(summary = "Get a list of all the rdf:Property within an artefact.", description = "Retrieves a list of all the rdf:Property within an artefact.", tags={ "Artefact" })
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = ""),
//
//            @ApiResponse(responseCode = "404", description = "") })
//    @GetMapping("/{artefactID}/resources/properties")
//    public ResponseEntity<List<RDFResource>> getArtefactProperties(
//            @PathVariable String artefactID,
//            @RequestParam(required = false, defaultValue = "json") ResponseFormat format,
//            @RequestParam(defaultValue = "1") int page,
//            @RequestParam(defaultValue = "50") int pagesize) {
//        return ResponseEntity.ok(List.of());
//    }
//
//
//
//
//    @Operation(summary = "Get a list of all the instances (owl individuals) within an artefact.", description = "Retrieves a list of all the instances (owl individuals) within an artefact.", tags={ "Artefact" })
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = ""),
//
//            @ApiResponse(responseCode = "404", description = "") })
//    @GetMapping("/{artefactID}/resources/individuals")
//    public ResponseEntity<List<RDFResource>> getArtefactIndividuals(
//            @PathVariable String artefactID,
//            @RequestParam(required = false, defaultValue = "json") ResponseFormat format,
//            @RequestParam(defaultValue = "1") int page,
//            @RequestParam(defaultValue = "50") int pagesize) {
//        return ResponseEntity.ok(List.of());
//    }
//
//
//    @Operation(summary = "Get a list of all the skos:Scheme within an artefact.", description = "Retrieves a list of all the skos:Scheme within an artefact.", tags={ "Artefact" })
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = ""),
//
//            @ApiResponse(responseCode = "404", description = "") })
//    @GetMapping("/{artefactID}/resources/schemes")
//    public ResponseEntity<List<RDFResource>> getArtefactSchemes(
//            @PathVariable String artefactID,
//            @RequestParam(required = false, defaultValue = "json") ResponseFormat format,
//            @RequestParam(defaultValue = "1") int page,
//            @RequestParam(defaultValue = "50") int pagesize) {
//        return ResponseEntity.ok(List.of());
//    }
//
//
//    @Operation(summary = "Get a list of all the skos:Collection within an artefact.", description = "Retrieves a list of all the skos:Collection within an artefact.", tags={ "Artefact" })
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = ""),
//
//            @ApiResponse(responseCode = "404", description = "") })
//    @GetMapping("/{artefactID}/resources/collection")
//    public ResponseEntity<List<RDFResource>> getArtefactCollections(
//            @PathVariable String artefactID,
//            @RequestParam(required = false, defaultValue = "json") ResponseFormat format,
//            @RequestParam(defaultValue = "1") int page,
//            @RequestParam(defaultValue = "50") int pagesize) {
//        return ResponseEntity.ok(List.of());
//    }

}