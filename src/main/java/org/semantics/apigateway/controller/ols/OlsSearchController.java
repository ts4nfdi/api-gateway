package org.semantics.apigateway.controller.ols;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.QueryParam;
import org.semantics.apigateway.model.CommonRequestParams;
import org.semantics.apigateway.model.TargetDbSchema;
import org.semantics.apigateway.service.artefacts.ArtefactsDataService;
import org.semantics.apigateway.service.search.SearchService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/ols")
@Tag(name = "Ols")
public class OlsSearchController {

    private final SearchService searchService;
    private final ArtefactsDataService artefactsService;

    public OlsSearchController(SearchService searchService, ArtefactsDataService artefactsService) {
        this.searchService = searchService;
        this.artefactsService = artefactsService;
    }

    @CrossOrigin
    @GetMapping("/api/select")
    public Object performDynFederatedSearchInOLSTargetDBSchema(@RequestParam Map<String, String> allParams) {
        String query;
        if (allParams.containsKey("q") || allParams.containsKey("query")) {
            if (allParams.containsKey("q")) {
                query = allParams.get("q");
            } else {
                query = allParams.get("query");
            }
        } else {
            query = "*";
        }

        return searchService.performSearch(query + "*", allParams.get("database"),  "ols", false);
    }

    @CrossOrigin
    @GetMapping("/api/ontologies/{ontology}/terms")
    public Object getTermsInOLSTargetDBSchema(@PathVariable String ontology, @QueryParam("iri") String iri, @ParameterObject CommonRequestParams params) {
        params.setTargetDbSchema(TargetDbSchema.ols);
        return this.artefactsService.getArtefactTerm(ontology, iri, params,  null);
    }
}
