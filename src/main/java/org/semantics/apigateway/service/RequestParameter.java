package org.semantics.apigateway.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static org.semantics.apigateway.service.RequestParameter.Type.*;

@AllArgsConstructor
public enum RequestParameter {
  
  
  // Commonly available parameters
  
  artefact(common),
  resourceUri(common),
  query(common),
  
  offset(common),
  page(common),
  size(common),
  pageSize(common),
  
  apiKey(common),
  
  // Parameters supported only by some backend types
  
  childrenOf(backendSpecific),
  allChildrenOf(backendSpecific),
  lang(backendSpecific),
  rows(backendSpecific),
  start(backendSpecific);
  
  @Getter
  Type type;
  
  public enum Type {common, backendSpecific}

}
