package org.semantics.apigateway.artefacts.tree;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.semantics.apigateway.model.CommonRequestParams;
import org.semantics.apigateway.service.auth.AuthService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;


@RestController
@RequestMapping("/artefacts/{id}")
@CrossOrigin
@Tag(name = "MOD - Artefact Data")
public class ArtefactsDataTreeController {
    private final ArtefactsDataTreeService artefactsService;
    private final AuthService authService;

    public ArtefactsDataTreeController(ArtefactsDataTreeService artefactsService,  AuthService authService) {
        this.artefactsService = artefactsService;
        this.authService = authService;
    }

    @GetMapping(value = {"/resources/concepts/roots", "/resources/classes/roots"})
    @Operation(summary = "List the top-level classes or concepts of an artefact.",
            description = "Returns the roots of the class or concept hierarchy of the artefact, i.e. the most general classes or concepts that have no parent " +
                    "(in SKOS: the top concepts). This is the starting point for browsing an artefact as a tree; " +
                    "use `.../children` to go one level deeper.\n\n" +
                    "Example: `GET /artefacts/ncit/resources/classes/roots`")
    public Object getArtefactRoots(@Parameter(description = "Short name of the artefact (`short_form` in `GET /artefacts`)", example = "ncit") @PathVariable String id,
                                   @ParameterObject CommonRequestParams params) {
        return this.artefactsService.getRoots(id, params, null, authService.tryGetCurrentUser());
    }

    @GetMapping(value = {"/resources/classes/children", "/resources/concepts/children"})
    @Operation(summary = "List the direct children of a class or concept.",
            description = "Returns the direct subclasses (OWL) or narrower concepts (SKOS) of the given class or concept, page by page, i.e. one level down in the hierarchy. " +
                    "**Requires** the `database` parameter with the name of exactly one source (e.g. `database=ebi`), because the hierarchy is read from that source.\n\n" +
                    "Example: `GET /artefacts/ncit/resources/classes/children?uri=http://purl.obolibrary.org/obo/NCIT_C2991&database=ebi`")
    public Object getConceptChildren(@Parameter(description = "Short name of the artefact (`short_form` in `GET /artefacts`)", example = "ncit") @PathVariable String id,
                                     @Parameter(description = "Full IRI of the parent class or concept", example = "http://purl.obolibrary.org/obo/NCIT_C2991") @RequestParam String uri,
                                     @ModelAttribute CommonRequestParams params,
                                     @Parameter(description = "Page number, starting at 1") @RequestParam(required = false, defaultValue = "1") Integer page) {
        uri = URLEncoder.encode(uri);
        return this.artefactsService.getChildren(id, uri, params, page, null, authService.tryGetCurrentUser());
    }



    @GetMapping(value = {"/resources/classes/tree", "/resources/concepts/tree"})
    @Operation(summary = "Get the hierarchy around a class or concept as a tree.",
            description = "Returns a nested tree (`children` field) that shows where the given class or concept is located in the hierarchy of the artefact, " +
                    "e.g. to display it in an expandable tree view. " +
                    "**Requires** the `database` parameter with the name of exactly one source (e.g. `database=ebi`), because the hierarchy is read from that source.\n\n" +
                    "Example: `GET /artefacts/ncit/resources/classes/tree?uri=http://purl.obolibrary.org/obo/NCIT_C2984&database=ebi`")
    public Object getConceptFullTree(@Parameter(description = "Short name of the artefact (`short_form` in `GET /artefacts`)", example = "ncit") @PathVariable String id,
                                     @Parameter(description = "Full IRI of the class or concept", example = "http://purl.obolibrary.org/obo/NCIT_C2984") @RequestParam String uri,
                                     @ModelAttribute CommonRequestParams params) {
        uri = URLEncoder.encode(uri);
        return this.artefactsService.getTree(id, uri, params, null, authService.tryGetCurrentUser());
    }
}
