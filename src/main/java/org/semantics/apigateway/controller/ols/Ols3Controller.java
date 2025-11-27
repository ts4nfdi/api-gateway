package org.semantics.apigateway.controller.ols;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.QueryParam;
import org.semantics.apigateway.artefacts.data.ArtefactsDataService;
import org.semantics.apigateway.artefacts.metadata.ArtefactsService;
import org.semantics.apigateway.artefacts.search.SearchService;
import org.semantics.apigateway.model.CommonRequestParams;
import org.semantics.apigateway.model.TargetDbSchema;
import org.semantics.apigateway.model.responses.AggregatedApiResponse;
import org.semantics.apigateway.model.user.User;
import org.semantics.apigateway.service.auth.AuthService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/ols")
@Tag(name = "OLS")
public class Ols3Controller {
  
  private final SearchService searchService;
  private final ArtefactsService artefactsService;
  private final ArtefactsDataService artefactsDataService;
  private final AuthService authService;
  
  public Ols3Controller(SearchService searchService, ArtefactsService artefactsService, ArtefactsDataService artefactsDataService, AuthService authService) {
    this.searchService = searchService;
    this.artefactsService = artefactsService;
    this.artefactsDataService = artefactsDataService;
    this.authService = authService;
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
    
    AggregatedApiResponse response = searchService.performSearch(query + "*", allParams.get("database"), "ols", false);
    return response.getCollection().getFirst();
  }
  
  @CrossOrigin
  @GetMapping("/api/ontologies/{ontology}/terms")
  public Object getTermsInOLSTargetDBSchema(@PathVariable String ontology, @RequestParam(required = false, defaultValue = "1") Integer page, @QueryParam("iri") String iri, @ParameterObject CommonRequestParams params) {
    params.setTargetDbSchema(TargetDbSchema.ols);
    if (iri != null) {
      return this.artefactsDataService.getArtefactTerm(ontology, iri, params, null);
    }
    return this.artefactsDataService.getArtefactTerms(ontology, params, page, null);
  }
  
  @CrossOrigin
  @GetMapping("/api/ontologies/{ontology}")
  public Object getArtefactMetadataInOLSTargetDBSchema(@PathVariable String ontology, @ParameterObject CommonRequestParams params) {
    params.setTargetDbSchema(TargetDbSchema.ols);
    return this.artefactsService.getArtefact(ontology, params, null);
  }
  
  @CrossOrigin
  @GetMapping("/api/ontologies")
  public Object getArtefactsInOLSTargetDBSchema(@ParameterObject CommonRequestParams params,
                                                @Parameter(description = "Collection id to browse terminologies in") @RequestParam(required = false) String collectionId) {
    params.setTargetDbSchema(TargetDbSchema.ols);
    User user = authService.tryGetCurrentUser();
    return this.artefactsService.getArtefacts(params, collectionId, user, null);
  }
}
