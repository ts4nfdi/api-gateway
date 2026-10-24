package org.semantics.apigateway.controller.ols;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.QueryParam;
import org.apache.commons.lang3.NotImplementedException;
import org.semantics.apigateway.artefacts.data.ArtefactsDataService;
import org.semantics.apigateway.artefacts.metadata.ArtefactsService;
import org.semantics.apigateway.artefacts.search.SearchService;
import org.semantics.apigateway.artefacts.tree.ArtefactsDataTreeService;
import org.semantics.apigateway.controller.ols.model.CommonOLS4Params;
import org.semantics.apigateway.model.CommonRequestParams;
import org.semantics.apigateway.service.RequestParameter;
import org.semantics.apigateway.service.auth.AuthService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Component
@RestController
@RequestMapping(value={"/ols/api/v2", "/ols4/api/v2"})
@Tag(name = "OLS 4")
@SuppressWarnings("unused")
public class Ols4Controller {
  
  private final SearchService searchService;
  private final ArtefactsService artefactsService;
  private final ArtefactsDataService artefactsDataService;
  private final AuthService authService;
  private final ArtefactsDataTreeService treeService;
  
  public Ols4Controller(SearchService searchService, ArtefactsService artefactsService, ArtefactsDataService artefactsDataService, AuthService authService, ArtefactsDataTreeService treeService) {
    this.searchService = searchService;
    this.artefactsService = artefactsService;
    this.artefactsDataService = artefactsDataService;
    this.authService = authService;
    this.treeService = treeService;
  }
  
  @CrossOrigin
  @GetMapping("/ontologies")
  public Object getAllOntologiesInOLSTargetDBSchema(@ParameterObject CommonRequestParams params, @ParameterObject CommonOLS4Params ols4Params, @ParameterObject @PageableDefault(page = 0, size = 20) Pageable pageable) {
    return artefactsService.getArtefacts(params, authService.tryGetCurrentUser(), null);
  }
  
  @CrossOrigin
  @GetMapping("/ontologies/{onto}")
  public Object getOntologyInOLSTargetDBSchema(@PathVariable String onto, @ParameterObject CommonRequestParams params) {
    return artefactsService.getArtefact(onto, params, null, authService.tryGetCurrentUser());
  }
  
  @CrossOrigin
  @GetMapping("/stats")
  public Object getStatsInOLSTargetDBSchema(@ParameterObject CommonRequestParams params) {
    throw new NotImplementedException();
  }
  
  @CrossOrigin
  @GetMapping("/ontologies/{onto}/individuals")
  public Object getAllIndividualsForOntologyInOLSTargetDBSchema(@PathVariable String onto, @ParameterObject CommonRequestParams params, @ParameterObject CommonOLS4Params ols4Params, @ParameterObject @PageableDefault(page = 0, size = 20) Pageable pageable, @RequestParam(name = "iri", required = false) String iri) {
    if (iri == null) return artefactsDataService.getArtefactIndividuals(onto, new HashMap<>(), params, pageable.getPageNumber() + 1, null, authService.tryGetCurrentUser());
    return artefactsDataService.getArtefactIndividual(onto, iri, new HashMap<>(), params, null, authService.tryGetCurrentUser());
  }
  
  @CrossOrigin
  @GetMapping("/ontologies/{onto}/individuals/{individual}")
  public Object getIndividualForOntologyInOLSTargetDBSchema(@PathVariable String onto, @PathVariable String individual, @ParameterObject CommonRequestParams params) {
    return artefactsDataService.getArtefactIndividual(onto, individual, new HashMap<>(), params, null, authService.tryGetCurrentUser());
  }
  
  @CrossOrigin
  @GetMapping("/ontologies/{onto}/classes/{class}/individuals")
  public Object getAllIndividualsForClassInOLSTargetDBSchema(@PathVariable String onto, @PathVariable("class") String clazz, @ParameterObject CommonRequestParams params, @ParameterObject CommonOLS4Params ols4Params, @ParameterObject @PageableDefault(page = 0, size = 20) Pageable pageable) {
    throw new NotImplementedException();
  }
  
  @Hidden
  @CrossOrigin
  @GetMapping("/individuals")
  public Object getAllIndividualsInOLSTargetDBSchema(@ParameterObject CommonRequestParams params, @ParameterObject CommonOLS4Params ols4Params, @ParameterObject @PageableDefault(page = 0, size = 20) Pageable pageable, @RequestParam(name = "iri", required = false) String iri) {
    return HttpStatus.NOT_FOUND;
  }
  
  @CrossOrigin
  @GetMapping("/ontologies/{onto}/entities")
  public Object getAllEntitiesForOntologyInOLSTargetDBSchema(@PathVariable String onto, @ParameterObject CommonRequestParams params, @ParameterObject CommonOLS4Params ols4Params, @PageableDefault(page = 0, size = 20) Pageable pageable, @QueryParam("iri") String iri, @RequestParam(name = "hierarchicalParent", required = false) String hierarchicalParent, @RequestParam(name = "hierarchicalAncestor", required = false) String hierarchicalAncestor) {
    Map<RequestParameter, String> apiParameters = new HashMap<>();
    apiParameters.put(RequestParameter.childrenOf, hierarchicalParent);
    apiParameters.put(RequestParameter.allChildrenOf, hierarchicalAncestor);
    if (iri == null) return artefactsDataService.getArtefactEntities(onto, apiParameters, params, pageable.getPageNumber() + 1, null, authService.tryGetCurrentUser());
    return artefactsDataService.getArtefactEntity(onto, iri, new HashMap<>(), params, null, authService.tryGetCurrentUser());
  }
  
  @CrossOrigin
  @GetMapping("/ontologies/{onto}/entities/{entity}")
  public Object getEntityInOLSTargetDBSchema(@PathVariable String onto, @PathVariable String entity, @ParameterObject CommonRequestParams params) {
    return artefactsDataService.getArtefactEntity(onto, entity, new HashMap<>(), params, null, authService.tryGetCurrentUser());
  }
  
  @CrossOrigin
  @GetMapping("/ontologies/{onto}/entities/{entity}/relatedFrom")
  public Object getEntityRelatedFromInOLSTargetDBSchema(@PathVariable String onto, @PathVariable String entity, @ParameterObject CommonRequestParams params, @ParameterObject CommonOLS4Params ols4Params, @ParameterObject @PageableDefault(page = 0, size = 20) Pageable pageable) {
    throw new NotImplementedException();
  }
  
  @Hidden
  @CrossOrigin
  @GetMapping("/entities")
  public Object getAllEntitiesInOLSTargetDBSchema(@ParameterObject CommonRequestParams params, @ParameterObject CommonOLS4Params ols4Params, @PageableDefault(page = 0, size = 20) Pageable pageable,
                                                  @QueryParam("iri") String iri,
                                                  @RequestParam(name = "hierarchicalParent", required = false) String hierarchicalParent,
                                                  @RequestParam(name = "hierarchicalAncestor", required = false) String hierarchicalAncestor) {
    // TODO Is there a way to run a federated query over all endpoints and their respective artifacts for all entities? Improbable, solely for performance reasons.
    Map<RequestParameter, String> apiParameters = new HashMap<>();
    apiParameters.put(RequestParameter.childrenOf, hierarchicalParent);
    apiParameters.put(RequestParameter.allChildrenOf, hierarchicalAncestor);
    if (iri != null) {
      return this.artefactsDataService.getArtefactEntities(iri, apiParameters, params, pageable.getPageNumber() + 1, null, authService.tryGetCurrentUser());
    }
    if (ols4Params.getSearch() != null) {
      return this.searchService.performSearch(ols4Params.getSearch(), params, authService.tryGetCurrentUser(), null, apiParameters);
    }
    return this.artefactsDataService.getArtefactEntities("", apiParameters, params, pageable.getPageNumber() + 1, null, authService.tryGetCurrentUser());
  }
  
  @CrossOrigin
  @GetMapping("/ontologies/{onto}/individuals/{individual}/ancestors")
  public Object getIndividualAncestorsInOLSTargetDBSchema(@PathVariable String onto, @PathVariable String individual, @ParameterObject CommonRequestParams params, @ParameterObject CommonOLS4Params ols4Params, @ParameterObject @PageableDefault(page = 0, size = 20) Pageable pageable) {
    throw new NotImplementedException();
  }
  
  @CrossOrigin
  @GetMapping("/ontologies/{onto}/classes")
  public Object getClassesInOLSTargetDBSchema(@PathVariable String onto, @ParameterObject CommonRequestParams params, @ParameterObject CommonOLS4Params ols4Params, @ParameterObject @PageableDefault(page = 0, size = 20) Pageable pageable, @RequestParam(name = "iri", required = false) String iri) {
    if (iri == null) return artefactsDataService.getArtefactTerms(onto, new HashMap<>(), params, pageable.getPageNumber() + 1, null, authService.tryGetCurrentUser());
    return artefactsDataService.getArtefactTerm(onto, iri, new HashMap<>(), params, null, authService.tryGetCurrentUser());
  }
  
  @CrossOrigin
  @GetMapping("/ontologies/{onto}/classes/{class}")
  public Object getClassInOLSTargetDBSchema(@PathVariable String onto, @PathVariable("class") String clazz, @ParameterObject CommonRequestParams params) {
    return artefactsDataService.getArtefactTerm(onto, clazz, new HashMap<>(), params, null, authService.tryGetCurrentUser());
  }
  
  @CrossOrigin
  @GetMapping("/ontologies/{onto}/classes/{class}/relatedFrom")
  public Object getClassRelatedFromInOLSTargetDBSchema(@PathVariable String onto, @PathVariable("class") String clazz, @ParameterObject CommonRequestParams params, @ParameterObject CommonOLS4Params ols4Params, @ParameterObject @PageableDefault(page = 0, size = 20) Pageable pageable) {
    throw new NotImplementedException();
  }
  
  @CrossOrigin
  @GetMapping("/ontologies/{onto}/classes/{class}/hierarchicalDescendants")
  public Object getClassHierarchicalDescendantsInOLSTargetDBSchema(@PathVariable String onto, @PathVariable("class") String clazz, @ParameterObject CommonRequestParams params, @ParameterObject CommonOLS4Params ols4Params, @ParameterObject @PageableDefault(page = 0, size = 20) Pageable pageable) {
    throw new NotImplementedException();
  }
  
  @CrossOrigin
  @GetMapping("/ontologies/{onto}/classes/{class}/hierarchicalChildren")
  public Object getClassHierarchicalChildrenInOLSTargetDBSchema(@PathVariable String onto, @PathVariable("class") String clazz, @ParameterObject CommonRequestParams params, @ParameterObject CommonOLS4Params ols4Params, @ParameterObject @PageableDefault(page = 0, size = 20) Pageable pageable) {
    throw new NotImplementedException();
  }
  
  @CrossOrigin
  @GetMapping("/ontologies/{onto}/classes/{class}/hierarchicalAncestors")
  public Object getClassHierarchicalAncestorsInOLSTargetDBSchema(@PathVariable String onto, @PathVariable("class") String clazz, @ParameterObject CommonRequestParams params, @ParameterObject CommonOLS4Params ols4Params, @ParameterObject @PageableDefault(page = 0, size = 20) Pageable pageable) {
    throw new NotImplementedException();
  }
  
  @CrossOrigin
  @GetMapping("/ontologies/{onto}/classes/{class}/descendants")
  public Object getClassDecendantsInOLSTargetDBSchema(@PathVariable String onto, @PathVariable("class") String clazz, @ParameterObject CommonRequestParams params, @ParameterObject CommonOLS4Params ols4Params, @ParameterObject @PageableDefault(page = 0, size = 20) Pageable pageable) {
    throw new NotImplementedException();
  }
  
  @CrossOrigin
  @GetMapping("/ontologies/{onto}/classes/{class}/children")
  public Object getClassChildrenInOLSTargetDBSchema(@PathVariable String onto, @PathVariable("class") String clazz, @ParameterObject CommonRequestParams params, @ParameterObject CommonOLS4Params ols4Params, @ParameterObject @PageableDefault(page = 0, size = 20) Pageable pageable) {
    return treeService.getChildren(onto, clazz, new HashMap<>(), params, pageable.getPageNumber() + 1, null, authService.tryGetCurrentUser());
  }
  
  @CrossOrigin
  @GetMapping("/ontologies/{onto}/classes/{class}/ancestors")
  public Object getClassAncestorsInOLSTargetDBSchema(@PathVariable String onto, @PathVariable("class") String clazz, @ParameterObject CommonRequestParams params, @ParameterObject CommonOLS4Params ols4Params, @ParameterObject @PageableDefault(page = 0, size = 20) Pageable pageable) {
    throw new NotImplementedException();
  }
  
  @Hidden
  @CrossOrigin
  @GetMapping("/classes")
  public Object getAllClassesInOLSTargetDBSchema(@ParameterObject CommonRequestParams params, @ParameterObject CommonOLS4Params ols4Params, @ParameterObject @PageableDefault(page = 0, size = 20) Pageable pageable, @RequestParam(name = "iri", required = false) String iri) {
    return HttpStatus.NOT_FOUND;
  }
  
  @Hidden
  @CrossOrigin
  @GetMapping("/properties")
  public Object getAllPropertiesInOLSTargetDBSchema(@ParameterObject CommonRequestParams params, @ParameterObject CommonOLS4Params ols4Params, @ParameterObject @PageableDefault(page = 0, size = 20) Pageable pageable, @RequestParam(name = "iri", required = false) String iri) {
    return HttpStatus.NOT_FOUND;
  }
  
  @CrossOrigin
  @GetMapping("/ontologies/{onto}/properties")
  public Object getPropertiesForOntologyInOLSTargetDBSchema(@PathVariable String onto, @ParameterObject CommonRequestParams params, @ParameterObject CommonOLS4Params ols4Params, @ParameterObject @PageableDefault(page = 0, size = 20) Pageable pageable, @RequestParam(name = "iri", required = false) String iri) {
    if (iri == null) return artefactsDataService.getArtefactProperties(onto, new HashMap<>(), params, pageable.getPageNumber() + 1, null, authService.tryGetCurrentUser());
    return artefactsDataService.getArtefactProperty(onto, iri, new HashMap<>(), params, null, authService.tryGetCurrentUser());
  }
  
  @CrossOrigin
  @GetMapping("/ontologies/{onto}/properties/{property}")
  public Object getPropertyInOLSTargetDBSchema(@PathVariable String onto, @PathVariable String property, @ParameterObject CommonRequestParams params) {
    return  artefactsDataService.getArtefactProperty(onto, property, new HashMap<>(), params, null, authService.tryGetCurrentUser());
  }
  
  @CrossOrigin
  @GetMapping("/ontologies/{onto}/properties/{property}/children")
  public Object getPropertyChildrenInOLSTargetDBSchema(@PathVariable String onto, @PathVariable String property, @ParameterObject CommonRequestParams params, @ParameterObject CommonOLS4Params ols4Params, @ParameterObject @PageableDefault(page = 0, size = 20) Pageable pageable) {
    return treeService.getChildren(onto, property, new HashMap<>(), params, pageable.getPageNumber() + 1, null, authService.tryGetCurrentUser());
  }
  
  @CrossOrigin
  @GetMapping("/ontologies/{onto}/properties/{property}/ancestors")
  public Object getPropertyAncestorsInOLSTargetDBSchema(@PathVariable String onto, @PathVariable String property, @ParameterObject CommonRequestParams params, @ParameterObject CommonOLS4Params ols4Params, @ParameterObject @PageableDefault(page = 0, size = 20) Pageable pageable) {
    throw new NotImplementedException();
  }
  
  @Hidden
  @CrossOrigin
  @GetMapping("/defined-fields")
  public Object getDefinedFieldsInOLSTargetDBSchema(@ParameterObject CommonRequestParams params) {
    throw new NotImplementedException();
  }
}
