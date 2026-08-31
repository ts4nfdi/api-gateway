package org.semantics.apigateway.service;

public enum RequestParameter {
  
  // Commonly available parameters
  
  artefact,
  resourceUri,
  query,
  
  offset,
  page,
  size,
  pageSize,
  
  apiKey,
  
  // Parameters supported only by some backend types
  
  childrenOf,
  allChildrenOf,
  lang,
  rows,
  start
}
