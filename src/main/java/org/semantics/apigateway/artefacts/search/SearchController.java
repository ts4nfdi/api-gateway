package org.semantics.apigateway.artefacts.search;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.semantics.apigateway.artefacts.metadata.ArtefactsService;
import org.semantics.apigateway.model.CommonRequestParams;
import org.semantics.apigateway.model.user.User;
import org.semantics.apigateway.service.auth.AuthService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/")
@CrossOrigin
@Tag(name = "MOD - Search")
public class SearchController {
    private final SearchService searchService;
    private final ArtefactsService artefactsService;
    private final AuthService authService;

    public SearchController(SearchService searchService, ArtefactsService artefactsService, AuthService authService) {
        this.searchService = searchService;
        this.artefactsService = artefactsService;
        this.authService = authService;
    }


    @Operation(summary = "Search for entities in all connected providers.",
            description = "Searches the *content* of the artefacts, i.e. entities such as classes and concepts, by their labels, in all connected providers at once, " +
                    "and returns one combined result list. Each result contains, among others, the term's `iri`, `label`, the artefact it belongs to (`ontology`), " +
                    "and the provider it comes from (`provider_name`). " +
                    "Use `database` to search only some providers and `collectionId` to search only the terminologies of a terminology collection. " +
                    "If you are logged in, your private terminology collections can be used as well. " +
                    "To search for whole artefacts instead of entities, use `GET /search/metadata`.\n\n" +
                    "Example: `GET /search?query=plant&database=ontoportal`")
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping(value = {"/search", "/search/content"})
    public Object search(
            @Parameter(description = "The text to search for in the labels of the entities", example = "plant")
            @RequestParam String query,
            @ParameterObject CommonRequestParams params
    ) {
        User user = authService.tryGetCurrentUser();
        return searchService.performSearch(query, params, user, null);
    }


    @Operation(summary = "Search for artefacts in all connected providers.",
            description = "Searches the *metadata* of the artefacts, i.e. finds whole ontologies, thesauri, or vocabularies (not single entities). " +
                    "Returns all artefacts whose label or IRI contains the search text (case-insensitive). " +
                    "To search for single entities instead, use `GET /search`.\n\n" +
                    "Example: `GET /search/metadata?query=plant`")
    @GetMapping(value = {"/search/metadata"})
    public Object searchMetadata(
            @Parameter(description = "The text to search for in the labels and IRIs of the artefacts", example = "plant")
            @RequestParam String query,
            @ParameterObject CommonRequestParams params
    ) {
        return artefactsService.searchMetadata(query, params, null);
    }
}
