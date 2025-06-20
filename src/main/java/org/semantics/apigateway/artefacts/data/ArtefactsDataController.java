package org.semantics.apigateway.artefacts.data;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.semantics.apigateway.model.CommonRequestParams;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;


@RestController
@RequestMapping("/artefacts/{id}")
@CrossOrigin
@Tag(name = "Artefacts / Data")
public class ArtefactsDataController {
    private final ArtefactsDataService artefactsService;

    public ArtefactsDataController(ArtefactsDataService artefactsService) {
        this.artefactsService = artefactsService;
    }

    @GetMapping(value = {"/resources/classes", "/resources/concepts"})
    @Operation(summary = "Get a list of all owl:Classes or skos:Concepts within an artefact.")
    public Object getArtefactTerms(@PathVariable String id, @RequestParam(required = false, defaultValue = "1") Integer page, @ParameterObject CommonRequestParams params) throws ExecutionException, InterruptedException {
        return this.artefactsService.getArtefactTerms(id, params, page, null);
    }

    @GetMapping(value = {"/resources/properties"})
    @Operation(summary = "Get a list of all rdf:Property")
    public Object getProperties(@PathVariable String id, @ModelAttribute CommonRequestParams params, @RequestParam(required = false, defaultValue = "1") Integer page) {
        return this.artefactsService.getArtefactProperties(id, params, page, null);
    }

    @GetMapping(value = {"/resources/individuals"})
    @Operation(summary = "Get a list of all owl:Instance")
    public Object getIndividuals(@PathVariable String id, @ModelAttribute CommonRequestParams params, @RequestParam(required = false, defaultValue = "1") Integer page) {
        return this.artefactsService.getArtefactIndividuals(id, params, page, null);
    }

    @GetMapping(value = { "/resources/classes/{uri}", "/resources/concepts/{uri}"})
    @Operation(summary = "Get a list of all owl:Classes or skos:Concepts within an artefact.")
    public Object getArtefactTerms(@PathVariable String id, @PathVariable String uri, @ParameterObject CommonRequestParams params) {
        return this.artefactsService.getArtefactTerm(id, uri, params, null);
    }

    @GetMapping(value = {"/resources/properties/{uri}"})
    @Operation(summary = "Get an rdf:Property details")
    public Object getPropertyDetails(@PathVariable String id, @PathVariable String uri, @ParameterObject CommonRequestParams params) {
        return this.artefactsService.getArtefactProperty(id, uri, params, null);
    }

    @GetMapping(value = {"/resources/individuals/{uri}"})
    @Operation(summary = "Get an owl:Instance details")
    public Object getIndividualDetails(@PathVariable String id, @PathVariable String uri, @ParameterObject CommonRequestParams params) {
        return this.artefactsService.getArtefactIndividual(id, uri, params, null);
    }

    @GetMapping(value = { "/resources/schemes"})
    @Operation(summary = "Get a list of all skos:ConceptSchemes within an artefact.")
    public Object getSchemes(@PathVariable String id, @ParameterObject CommonRequestParams params, @RequestParam(required = false, defaultValue = "1") Integer page) {
        return this.artefactsService.getArtefactSchemes(id, params, page, null);
    }

    @GetMapping(value = {"/resources/schemes/{uri}"})
    @Operation(summary = "Get a skos:ConceptScheme details")
    public Object getSchemeDetails(@PathVariable String id, @PathVariable String uri, @ParameterObject CommonRequestParams params) {
        return this.artefactsService.getArtefactScheme(id, uri, params, null);
    }

    @GetMapping(value = {"/resources/collections"})
    @Operation(summary = "Get a list of all skos:Collections within an artefact.")
    public Object getCollections(@PathVariable String id, @ParameterObject CommonRequestParams params, @RequestParam(required = false, defaultValue = "1") Integer page) {
        return this.artefactsService.getArtefactCollections(id, params, page, null);
    }

    @GetMapping(value = { "/resources/collections/{uri}"})
    @Operation(summary = "Get a skos:Collection details")
    public Object getCollectionDetails(@PathVariable String id, @PathVariable String uri, @ParameterObject CommonRequestParams params) {
        return this.artefactsService.getArtefactCollection(id, uri, params, null);
    }
}
