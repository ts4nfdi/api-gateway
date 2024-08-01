package org.semantics.apigateway.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.semantics.apigateway.model.*;
import org.semantics.apigateway.service.ArtefactsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/catalogs")
public class SemanticArtefactCatalogsController {


    @Operation(summary = "Get information about the semantic artefact catalogue.", description = "", tags={ "Catalog" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = ""),

            @ApiResponse(responseCode = "404", description = "") })
    @CrossOrigin
    @GetMapping
    public ResponseEntity<List<SemanticArtefactCatalog>> getCatalogs(){
        return null;
    }
}