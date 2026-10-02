package org.semantics.apigateway.controller.ols;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.QueryParam;
import org.semantics.apigateway.api.OlsTransformer;
import org.semantics.apigateway.artefacts.data.ArtefactsDataService;
import org.semantics.apigateway.artefacts.metadata.ArtefactsService;
import org.semantics.apigateway.artefacts.search.SearchService;
import org.semantics.apigateway.model.CommonRequestParams;
import org.semantics.apigateway.model.responses.AggregatedApiResponse;
import org.semantics.apigateway.model.user.User;
import org.semantics.apigateway.service.auth.AuthService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Component
@RestController
@RequestMapping(value={"/ols/api", "/ols4/api"})
@Tag(name = "OLS")
public class Ols3Controller {
  
  private final SearchService searchService;
  private final ArtefactsService artefactsService;
  private final ArtefactsDataService artefactsDataService;
  private final AuthService authService;

  private final OlsTransformer olsTransformer = new OlsTransformer(); // TODO This breaks decoupling. Better pass original request through the services, so that we know how to construct the response in the transformers.
  
  public Ols3Controller(SearchService searchService, ArtefactsService artefactsService, ArtefactsDataService artefactsDataService, AuthService authService) {
    this.searchService = searchService;
    this.artefactsService = artefactsService;
    this.artefactsDataService = artefactsDataService;
    this.authService = authService;
  }
  
  // TODO
  @CrossOrigin
  @GetMapping(value = {"/select", "/search"})
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
    
    long timeout;
    try {
      timeout = Long.parseLong(allParams.get("timeout"));
    } catch (Exception e) {
      timeout = 60 * 1000;
    }
    
    AggregatedApiResponse response = searchService.performSearch(query + "*", allParams.get("ontology"), "ols", allParams.get("collectionId"), false, timeout);
    return response.getCollection().get(0);
  }
  
  @CrossOrigin
  @GetMapping("/suggest")
  public Object performTermSuggestInOLSTargetDBSchema(@RequestParam String q, @RequestParam(required = false, defaultValue = "") String ontology, @RequestParam(required = false, defaultValue = "10") Integer rows, @RequestParam(required = false, defaultValue = "0") Integer start, @ParameterObject CommonRequestParams params) {
    return searchService.suggestConcepts(ontology, q, start + 1, rows, params);
  }
  
  @CrossOrigin
  @GetMapping("/terms")
  public Object getAllTermsInOLSTargetDBSchema(@RequestParam(required = false, defaultValue = "0") Integer page, @QueryParam("iri") String iri, @ParameterObject CommonRequestParams params) {
    if (iri != null) {
      return this.artefactsDataService.getArtefactTerms(iri, params, page + 1, null, authService.tryGetCurrentUser());
    }
    return this.artefactsDataService.getArtefactTerms("", params, page + 1, null, authService.tryGetCurrentUser());
  }
  
  @CrossOrigin
  @GetMapping("/ontologies/{onto}/terms")
  public Object getTermsInOLSTargetDBSchema(@PathVariable String onto, @RequestParam(required = false, defaultValue = "1") Integer page, @QueryParam("iri") String iri, @ParameterObject CommonRequestParams params) {
    if (iri != null) {
      return this.artefactsDataService.getArtefactTerm(onto, iri, params, null, authService.tryGetCurrentUser());
    }
    return this.artefactsDataService.getArtefactTerms(onto, params, page + 1, null, authService.tryGetCurrentUser());
  }
  
  @CrossOrigin
  @GetMapping("/ontologies/{onto}")
  public Object getArtefactMetadataInOLSTargetDBSchema(@PathVariable String onto, @ParameterObject CommonRequestParams params) {
    return this.artefactsService.getArtefact(onto, params, null, authService.tryGetCurrentUser());
  }
  
  @CrossOrigin
  @GetMapping("/ontologies")
  public Object getArtefactsInOLSTargetDBSchema(@ParameterObject CommonRequestParams params) {
    User user = authService.tryGetCurrentUser();
    return this.artefactsService.getArtefacts(params, user, null);
  }

  @CrossOrigin
  @GetMapping("/ontologies/{onto}/individuals")
  public Object getAllIndividualsForOntologyInOLSTargetDBSchema(@PathVariable String onto, @ParameterObject CommonRequestParams params, @PageableDefault(page = 0, size = 20) Pageable pageable, @QueryParam("iri") String iri) {
    if (iri == null) return artefactsDataService.getArtefactIndividuals(onto, params, pageable.getPageNumber() + 1, null, authService.tryGetCurrentUser());
    AggregatedApiResponse response = (AggregatedApiResponse) artefactsDataService.getArtefactIndividual(onto, iri, params, null, authService.tryGetCurrentUser());
    return olsTransformer.constructResponse(response.getCollection(), "individuals", true, true, 1, response.getCollection().size());
  }

  @CrossOrigin
  @GetMapping("/ontologies/{onto}/individuals/{individual}")
  public Object getIndividualForOntologyInOLSTargetDBSchema(@PathVariable String onto, @PathVariable String individual, @ParameterObject CommonRequestParams params) {
    return artefactsDataService.getArtefactIndividual(onto, individual, params, null, authService.tryGetCurrentUser());
  }

  @CrossOrigin
  @GetMapping("/ontologies/{onto}/properties")
  public Object getPropertiesForOntologyInOLSTargetDBSchema(@PathVariable String onto, @ParameterObject CommonRequestParams params, @PageableDefault(page = 0, size = 20) Pageable pageable, @QueryParam("iri") String iri) {
    if (iri == null) return artefactsDataService.getArtefactProperties(onto, params, pageable.getPageNumber() + 1, null, authService.tryGetCurrentUser());
    AggregatedApiResponse response = (AggregatedApiResponse) artefactsDataService.getArtefactProperty(onto, iri, params, null, authService.tryGetCurrentUser());
    return olsTransformer.constructResponse(response.getCollection(), "properties", true, true, 1, response.getCollection().size());
  }

  @CrossOrigin
  @GetMapping("/ontologies/{onto}/properties/{property}")
  public Object getPropertyInOLSTargetDBSchema(@PathVariable String onto, @PathVariable String property, @ParameterObject CommonRequestParams params) {
    return  artefactsDataService.getArtefactProperty(onto, property, params, null, authService.tryGetCurrentUser());
  }
}

