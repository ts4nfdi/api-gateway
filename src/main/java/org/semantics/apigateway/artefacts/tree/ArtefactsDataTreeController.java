package org.semantics.apigateway.artefacts.tree;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.semantics.apigateway.model.CommonRequestParams;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;


@RestController
@RequestMapping("/artefacts/{id}")
@CrossOrigin
@Tag(name = "Artefacts / Data")
public class ArtefactsDataTreeController {
    private final ArtefactsDataTreeService artefactsService;

    public ArtefactsDataTreeController(ArtefactsDataTreeService artefactsService) {
        this.artefactsService = artefactsService;
    }

    @GetMapping(value = {"/resources/concepts/roots", "/resources/classes/roots"})
    @Operation(summary = "Get a list of all roots in an artefact.")
    public Object getArtefactRoots(@PathVariable String id, @ParameterObject CommonRequestParams params) {
        return this.artefactsService.getRoots(id, params, null);
    }

    @GetMapping(value = {"/resources/classes/children", "/resources/concepts/children"})
    @Operation(summary = "Get a list of all children of a specific owl:Class or skos:Concept within an artefact.")
    public Object getConceptChildren(@PathVariable String id, @RequestParam String uri, @ModelAttribute CommonRequestParams params,
                                     @RequestParam(required = false, defaultValue = "1") Integer page) {
        uri = URLEncoder.encode(uri);
        return this.artefactsService.getChildren(id, uri, params, page, null);
    }



    @GetMapping(value = {"/resources/classes/children", "/resources/concepts/tree"})
    @Operation(summary = "Get a full tree of all children of a specific owl:Class or skos:Concept within an artefact.")
    public Object getConceptFullTree(@PathVariable String id, @RequestParam String uri, @ModelAttribute CommonRequestParams params) {
        uri = URLEncoder.encode(uri);
        return this.artefactsService.getTree(id, uri, params, null);
    }
}
