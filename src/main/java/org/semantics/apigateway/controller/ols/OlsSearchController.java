package org.semantics.apigateway.controller.ols;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.QueryParam;
import org.semantics.apigateway.artefacts.data.ArtefactsDataService;
import org.semantics.apigateway.artefacts.search.SearchService;
import org.semantics.apigateway.model.CommonRequestParams;
import org.semantics.apigateway.model.TargetSchema;
import org.semantics.apigateway.model.responses.AggregatedApiResponse;
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
    public Object performDynFederatedSearchInOLSTargetSchema(@RequestParam Map<String, String> allParams) {
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
        
        String source = allParams.get("source");
        if (source == null || source.isEmpty()) {source = allParams.get("database");}

        AggregatedApiResponse response = searchService.performSearch(query + "*", source, "ols", false);
        return response.getCollection().get(0);
    }

    @CrossOrigin
    @GetMapping("/api/ontologies/{ontology}/terms")
    public Object getTermsInOLSTargetSchema(@PathVariable String ontology, @QueryParam("iri") String iri, @ParameterObject CommonRequestParams params) {
        params.setTargetSchema(TargetSchema.ols);
        return this.artefactsService.getArtefactTerm(ontology, iri, params,  null);
    }
}
