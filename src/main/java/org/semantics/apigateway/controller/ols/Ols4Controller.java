package org.semantics.apigateway.controller.ols;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.NotImplementedException;
import org.semantics.apigateway.artefacts.data.ArtefactsDataService;
import org.semantics.apigateway.artefacts.metadata.ArtefactsService;
import org.semantics.apigateway.artefacts.search.SearchService;
import org.semantics.apigateway.artefacts.tree.ArtefactsDataTreeService;
import org.semantics.apigateway.controller.ols.model.CommonOLS4Params;
import org.semantics.apigateway.controller.ols.model.Pageable;
import org.semantics.apigateway.controller.ols.model.PageableEditor;
import org.semantics.apigateway.model.CommonRequestParams;
import org.semantics.apigateway.service.auth.AuthService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

@Component
@RestController
@RequestMapping("/ols/api/v2")
@Tag(name = "OLS 4")
@SuppressWarnings("unused")
public class Ols4Controller {
  
  private final ArtefactsService artefactsService;
  private final ArtefactsDataService artefactsDataService;
  private final AuthService authService;
  private final ArtefactsDataTreeService treeService;
  
  public Ols4Controller(SearchService searchService, ArtefactsService artefactsService, ArtefactsDataService artefactsDataService, AuthService authService, ArtefactsDataTreeService treeService) {
    this.artefactsService = artefactsService;
    this.artefactsDataService = artefactsDataService;
    this.authService = authService;
    this.treeService = treeService;
  }
  
  @InitBinder
  public void initBinder(WebDataBinder binder) {
    binder.registerCustomEditor(Pageable.class, new PageableEditor());
  }
  
  @CrossOrigin
  @GetMapping("/ontologies")
  public Object getAllOntologiesInOLSTargetDBSchema(@ParameterObject CommonRequestParams params, CommonOLS4Params ols4Params, @RequestParam(required = false) String collectionId) {
    return artefactsService.getArtefacts(params, collectionId, authService.tryGetCurrentUser(), null);
  }
  
  @CrossOrigin
  @GetMapping("/ontologies/{onto}")
  public Object getOntologyInOLSTargetDBSchema(@PathVariable String onto, @ParameterObject CommonRequestParams params) {
    return artefactsService.getArtefact(onto, params, null);
  }
  
  @CrossOrigin
  @GetMapping("/stats")
  public Object getStatsInOLSTargetDBSchema(@ParameterObject CommonRequestParams params) {
    throw new NotImplementedException();
  }
  
  @CrossOrigin
  @GetMapping("/ontologies/{onto}/individuals")
  public Object getAllIndividualsForOntologyInOLSTargetDBSchema(@PathVariable String onto, @ParameterObject CommonRequestParams params, CommonOLS4Params ols4Params) {
    return artefactsDataService.getArtefactIndividuals(onto, params, ols4Params.getPageable().getPage(), null);
  }
  
  @CrossOrigin
  @GetMapping("/ontologies/{onto}/individuals/{individual}")
  public Object getIndividualForOntologyInOLSTargetDBSchema(@PathVariable String onto, @PathVariable String individual, @ParameterObject CommonRequestParams params) {
    return artefactsDataService.getArtefactIndividual(onto, individual, params, null);
  }
  
  @CrossOrigin
  @GetMapping("/ontologies/{onto}/classes/{class}/individuals")
  public Object getAllIndividualsForClassInOLSTargetDBSchema(@PathVariable String onto, @PathVariable("class") String clazz, @ParameterObject CommonRequestParams params, @ParameterObject CommonOLS4Params ols4Params) {
    throw new NotImplementedException();
  }
  
  @CrossOrigin
  @GetMapping("/individuals")
  public Object getAllIndividualsInOLSTargetDBSchema(@ParameterObject CommonRequestParams params, @ParameterObject CommonOLS4Params ols4Params, @RequestParam(required = false) String collectionId) {
    throw new NotImplementedException();
  }
  
  @CrossOrigin
  @GetMapping("/ontologies/{onto}/entities")
  public Object getAllEntitiesForOntologyInOLSTargetDBSchema(@PathVariable String onto, @ParameterObject CommonRequestParams params, CommonOLS4Params ols4Params) {
    return artefactsDataService.getArtefactTerms(onto, params, ols4Params.getPageable().getPage(), null);
  }
  
  @CrossOrigin
  @GetMapping("/ontologies/{onto}/entities/{entity}")
  public Object getEntityInOLSTargetDBSchema(@PathVariable String onto, @PathVariable String entity, @ParameterObject CommonRequestParams params) {
    return artefactsDataService.getArtefactIndividual(onto, entity, params, null);
  }
  
  @CrossOrigin
  @GetMapping("/ontologies/{onto}/entities/{entity}/relatedFrom")
  public Object getEntityRelatedFromInOLSTargetDBSchema(@PathVariable String onto, @PathVariable String entity, @ParameterObject CommonRequestParams params, @ParameterObject CommonOLS4Params ols4Params) {
    throw new NotImplementedException();
  }
  
  @CrossOrigin
  @GetMapping("/entities")
  public Object getAllEntitiesInOLSTargetDBSchema(@ParameterObject CommonRequestParams params, @ParameterObject CommonOLS4Params ols4Params) {
    // TODO Is there a way to run a federated query over all endpoints and their respective artifacts for all entities? Improbable, solely for performance reasons.
    throw new NotImplementedException();
  }
  
  @CrossOrigin
  @GetMapping("/ontologies/{onto}/individuals/{individual}/ancestors")
  public Object getIndividualAncestorsInOLSTargetDBSchema(@PathVariable String onto, @PathVariable String individual, @ParameterObject CommonRequestParams params, @ParameterObject CommonOLS4Params ols4Params) {
    throw new NotImplementedException();
  }
  
  @CrossOrigin
  @GetMapping("/ontologies/{onto}/classes")
  public Object getClassesInOLSTargetDBSchema(@PathVariable String onto, @ParameterObject CommonRequestParams params, @ParameterObject CommonOLS4Params ols4Params) {
    return this.artefactsDataService.getArtefactTerms(onto, params, ols4Params.getPageable().getPage(), null);
  }
  
  @CrossOrigin
  @GetMapping("/ontologies/{onto}/classes/{class}")
  public Object getClassInOLSTargetDBSchema(@PathVariable String onto, @PathVariable("class") String clazz, @ParameterObject CommonRequestParams params) {
    return artefactsDataService.getArtefactTerm(onto, clazz, params, null);
  }
  
  @CrossOrigin
  @GetMapping("/ontologies/{onto}/classes/{class}/relatedFrom")
  public Object getClassRelatedFromInOLSTargetDBSchema(@PathVariable String onto, @PathVariable("class") String clazz, @ParameterObject CommonRequestParams params, @ParameterObject CommonOLS4Params ols4Params) {
    throw new NotImplementedException();
  }
  
  @CrossOrigin
  @GetMapping("/ontologies/{onto}/classes/{class}/hierarchicalDescendants")
  public Object getClassHierarchicalDescendantsInOLSTargetDBSchema(@PathVariable String onto, @PathVariable("class") String clazz, @ParameterObject CommonRequestParams params, @ParameterObject CommonOLS4Params ols4Params) {
    throw new NotImplementedException();
  }
  
  @CrossOrigin
  @GetMapping("/ontologies/{onto}/classes/{class}/hierarchicalChildren")
  public Object getClassHierarchicalChildrenInOLSTargetDBSchema(@PathVariable String onto, @PathVariable("class") String clazz, @ParameterObject CommonRequestParams params, @ParameterObject CommonOLS4Params ols4Params) {
    throw new NotImplementedException();
  }
  
  @CrossOrigin
  @GetMapping("/ontologies/{onto}/classes/{class}/hierarchicalAncestors")
  public Object getClassHierarchicalAncestorsInOLSTargetDBSchema(@PathVariable String onto, @PathVariable("class") String clazz, @ParameterObject CommonRequestParams params, @ParameterObject CommonOLS4Params ols4Params) {
    throw new NotImplementedException();
  }
  
  @CrossOrigin
  @GetMapping("/ontologies/{onto}/classes/{class}/descendants")
  public Object getClassDecendantsInOLSTargetDBSchema(@PathVariable String onto, @PathVariable("class") String clazz, @ParameterObject CommonRequestParams params, @ParameterObject CommonOLS4Params ols4Params) {
    throw new NotImplementedException();
  }
  
  @CrossOrigin
  @GetMapping("/ontologies/{onto}/classes/{class}/children")
  public Object getClassChildrenInOLSTargetDBSchema(@PathVariable String onto, @PathVariable("class") String clazz, @ParameterObject CommonRequestParams params, @ParameterObject CommonOLS4Params ols4Params) {
    return treeService.getChildren(onto, clazz, params, ols4Params.getPageable().getPage(), null);
  }
  
  @CrossOrigin
  @GetMapping("/ontologies/{onto}/classes/{class}/ancestors")
  public Object getClassAncestorsInOLSTargetDBSchema(@PathVariable String onto, @PathVariable("class") String clazz, @ParameterObject CommonRequestParams params, @ParameterObject CommonOLS4Params ols4Params) {
    throw new NotImplementedException();
  }
  
  @CrossOrigin
  @GetMapping("/classes")
  public Object getAllClassesInOLSTargetDBSchema(@ParameterObject CommonRequestParams params, @ParameterObject CommonOLS4Params ols4Params) {
    throw new NotImplementedException();
  }
  
  @CrossOrigin
  @GetMapping("/properties")
  public Object getAllPropertiesInOLSTargetDBSchema(@ParameterObject CommonRequestParams params, @ParameterObject CommonOLS4Params ols4Params) {
    throw new NotImplementedException();
  }
  
  @CrossOrigin
  @GetMapping("/ontologies/{onto}/properties")
  public Object getPropertiesForOntologyInOLSTargetDBSchema(@PathVariable String onto, @ParameterObject CommonRequestParams params, @ParameterObject CommonOLS4Params ols4Params) {
    return artefactsDataService.getArtefactProperties(onto, params, ols4Params.getPageable().getPage(), null);
  }
  
  @CrossOrigin
  @GetMapping("/ontologies/{onto}/properties/{property}")
  public Object getPropertyInOLSTargetDBSchema(@PathVariable String onto, @PathVariable String property, @ParameterObject CommonRequestParams params) {
    return  artefactsDataService.getArtefactProperty(onto, property, params, null);
  }
  
  @CrossOrigin
  @GetMapping("/ontologies/{onto}/properties/{property}/children")
  public Object getPropertyChildenInOLSTargetDBSchema(@PathVariable String onto, @PathVariable String property, @ParameterObject CommonRequestParams params, @ParameterObject CommonOLS4Params ols4Params) {
    return treeService.getChildren(onto, property, params, ols4Params.getPageable().getPage(), null);
  }
  
  @CrossOrigin
  @GetMapping("/ontologies/{onto}/properties/{property}/ancestors")
  public Object getProperyAncestorsInOLSTargetDBSchema(@PathVariable String onto, @PathVariable String property, @ParameterObject CommonRequestParams params, @ParameterObject CommonOLS4Params ols4Params) {
    throw new NotImplementedException();
  }
  
  @CrossOrigin
  @GetMapping("/defined-fields")
  public Object getDefinedFieldsInOLSTargetDBSchema(@ParameterObject CommonRequestParams params) {
    throw new NotImplementedException();
  }
}
