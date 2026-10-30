package org.semantics.apigateway.artefacts.data;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.semantics.apigateway.model.CommonRequestParams;
import org.semantics.apigateway.service.auth.AuthService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/artefacts/{id}")
@CrossOrigin
@Tag(name = "MOD - Artefact Data")
public class ArtefactsDataController {
    private final ArtefactsDataService artefactsService;
    private final AuthService authService;

    public ArtefactsDataController(ArtefactsDataService artefactsService,  AuthService authService) {
        this.artefactsService = artefactsService;
        this.authService = authService;
    }

    @GetMapping(value = {"/resources/classes", "/resources/concepts"})
    @Operation(summary = "List the classes or concepts of an artefact.",
            description = "Returns the classes (`owl:Class`, for OWL ontologies) or concepts (`skos:Concept`, for SKOS vocabularies) of the artefact. " +
                    "Classes and concepts are the main entries of a terminology, e.g. \"Neoplasm\" or \"Renewable energy\". " +
                    "Both paths return the same result.\n\n" +
                    "Example: `GET /artefacts/ncit/resources/classes?page=2`")
    public Object getArtefactTerms(@Parameter(description = "Short name or ID of the artefact", example = "ncit") @PathVariable String id,
                                   @Parameter(description = "Page number, starting at 1") @RequestParam(required = false, defaultValue = "1") Integer page,
                                   @ParameterObject CommonRequestParams params) {
        return this.artefactsService.getArtefactTerms(id, params, page, null, authService.tryGetCurrentUser());
    }

    @GetMapping(value = {"/resources/properties"})
    @Operation(summary = "List the properties of an artefact.",
            description = "Returns the properties (`rdf:Property`, including OWL object, datatype, and annotation properties) defined in the artefact. " +
                    "Properties describe relations between entities or attributes of entities, e.g. \"part of\" or \"has colour\".\n\n" +
                    "Example: `GET /artefacts/ncit/resources/properties`")
    public Object getProperties(@Parameter(description = "Short name or ID of the artefact", example = "ncit") @PathVariable String id,
                                @ModelAttribute CommonRequestParams params,
                                @Parameter(description = "Page number, starting at 1") @RequestParam(required = false, defaultValue = "1") Integer page) {
        return this.artefactsService.getArtefactProperties(id, params, page, null, authService.tryGetCurrentUser());
    }

    @GetMapping(value = {"/resources/individuals"})
    @Operation(summary = "List the individuals of an artefact.",
            description = "Returns the individuals (`owl:NamedIndividual`) of the artefact. " +
                    "An individual is a concrete member of a class, e.g. \"my car\" is an individual of the class \"Car\". " +
                    "Mainly relevant for OWL ontologies; SKOS vocabularies usually have no individuals.\n\n" +
                    "Example: `GET /artefacts/ncit/resources/individuals`")
    public Object getIndividuals(@Parameter(description = "Short name or ID of the artefact", example = "ncit") @PathVariable String id,
                                 @ModelAttribute CommonRequestParams params,
                                 @Parameter(description = "Page number, starting at 1") @RequestParam(required = false, defaultValue = "1") Integer page) {
        return this.artefactsService.getArtefactIndividuals(id, params, page, null, authService.tryGetCurrentUser());
    }

    @GetMapping(value = { "/resources/classes/{uri}", "/resources/concepts/{uri}"})
    @Operation(summary = "Get the details of one class or concept.",
            description = "Returns the details of one class or concept of the artefact, e.g. its labels, synonyms, descriptions, " +
                    "whether it is obsolete, and further annotations. " +
                    "The class or concept is identified by its full IRI, which must be URL-encoded in the path.\n\n" +
                    "Example: `GET /artefacts/ncit/resources/classes/http%3A%2F%2Fpurl.obolibrary.org%2Fobo%2FNCIT_C2984`")
    public Object getArtefactTerms(@Parameter(description = "Short name or ID of the artefact", example = "ncit") @PathVariable String id,
                                   @Parameter(description = "Full IRI of the class or concept, URL-encoded", example = "http%3A%2F%2Fpurl.obolibrary.org%2Fobo%2FNCIT_C2984") @PathVariable String uri,
                                   @ParameterObject CommonRequestParams params) {
        return this.artefactsService.getArtefactTerm(id, uri, params, null, authService.tryGetCurrentUser());
    }

    @GetMapping(value = {"/resources/properties/{uri}"})
    @Operation(summary = "Get the details of one property.",
            description = "Returns the details of one property of the artefact, e.g. its labels and descriptions. " +
                    "The property is identified by its full IRI, which must be URL-encoded in the path.")
    public Object getPropertyDetails(@Parameter(description = "Short name or ID of the artefact", example = "ncit") @PathVariable String id,
                                     @Parameter(description = "Full IRI of the property, URL-encoded") @PathVariable String uri,
                                     @ParameterObject CommonRequestParams params) {
        return this.artefactsService.getArtefactProperty(id, uri, params, null, authService.tryGetCurrentUser());
    }

    @GetMapping(value = {"/resources/individuals/{uri}"})
    @Operation(summary = "Get the details of one individual.",
            description = "Returns the details of one individual of the artefact, e.g. its labels and descriptions. " +
                    "The individual is identified by its full IRI, which must be URL-encoded in the path.")
    public Object getIndividualDetails(@Parameter(description = "Short name or ID of the artefact", example = "ncit") @PathVariable String id,
                                       @Parameter(description = "Full IRI of the individual, URL-encoded") @PathVariable String uri,
                                       @ParameterObject CommonRequestParams params) {
        return this.artefactsService.getArtefactIndividual(id, uri, params, null, authService.tryGetCurrentUser());
    }

    @GetMapping(value = { "/resources/schemes"})
    @Operation(summary = "List the concept schemes of a SKOS vocabulary.",
            description = "Returns the concept schemes (`skos:ConceptScheme`) of the artefact. " +
                    "A concept scheme is a whole vocabulary or one part of it that groups concepts, e.g. a thesaurus. " +
                    "Only SKOS vocabularies have concept schemes; for OWL ontologies the result is usually empty.\n\n" +
                    "Example: `GET /artefacts/agrovoc/resources/schemes`")
    public Object getSchemes(@Parameter(description = "Short name or ID of the artefact", example = "agrovoc") @PathVariable String id,
                             @ParameterObject CommonRequestParams params,
                             @Parameter(description = "Page number, starting at 1") @RequestParam(required = false, defaultValue = "1") Integer page) {
        return this.artefactsService.getArtefactSchemes(id, params, page, null, authService.tryGetCurrentUser());
    }

    @GetMapping(value = {"/resources/schemes/{uri}"})
    @Operation(summary = "Get the details of one concept scheme.",
            description = "Returns the details of one concept scheme (`skos:ConceptScheme`) of the artefact, e.g. its labels and descriptions. " +
                    "The concept scheme is identified by its full IRI, which must be URL-encoded in the path.")
    public Object getSchemeDetails(@Parameter(description = "Short name or ID of the artefact", example = "agrovoc") @PathVariable String id,
                                   @Parameter(description = "Full IRI of the concept scheme, URL-encoded") @PathVariable String uri,
                                   @ParameterObject CommonRequestParams params) {
        return this.artefactsService.getArtefactScheme(id, uri, params, null, authService.tryGetCurrentUser());
    }

    @GetMapping(value = {"/resources/collections"})
    @Operation(summary = "List the SKOS collections of a SKOS vocabulary.",
            description = "Returns the SKOS collections (`skos:Collection`) of the artefact. " +
                    "A SKOS collection is a labeled group of concepts within one vocabulary, used for organization or display, e.g. \"fruits\". " +
                    "Not to be confused with terminology collections (see **Collections**), which are sets of whole terminologies. " +
                    "Only SKOS vocabularies have SKOS collections; for OWL ontologies the result is usually empty.")
    public Object getCollections(@Parameter(description = "Short name or ID of the artefact", example = "agrovoc") @PathVariable String id,
                                 @ParameterObject CommonRequestParams params,
                                 @Parameter(description = "Page number, starting at 1") @RequestParam(required = false, defaultValue = "1") Integer page) {
        return this.artefactsService.getArtefactCollections(id, params, page, null, authService.tryGetCurrentUser());
    }

    @GetMapping(value = { "/resources/collections/{uri}"})
    @Operation(summary = "Get the details of one SKOS collection.",
            description = "Returns the details of one SKOS collection (`skos:Collection`) of the artefact, e.g. its labels and descriptions. " +
                    "The SKOS collection is identified by its full IRI, which must be URL-encoded in the path.")
    public Object getCollectionDetails(@Parameter(description = "Short name or ID of the artefact", example = "agrovoc") @PathVariable String id,
                                       @Parameter(description = "Full IRI of the SKOS collection, URL-encoded") @PathVariable String uri,
                                       @ParameterObject CommonRequestParams params) {
        return this.artefactsService.getArtefactCollection(id, uri, params, null, authService.tryGetCurrentUser());
    }
}
