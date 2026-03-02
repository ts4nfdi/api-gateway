package org.semantics.apigateway.controller.ols.model;

import com.fasterxml.jackson.databind.util.JSONPObject;
import jakarta.ws.rs.QueryParam;
import lombok.Data;

@Data
public class CommonOLS4Params {
  
  @QueryParam("search")
  private String search;
  
  @QueryParam("searchFields")
  private String searchFields;
  
  @QueryParam("boostFields")
  private String boostFields;
  
  @QueryParam("exactMatch")
  private boolean exactMatch = false;
  
  @QueryParam("includeObsoleteEntities")
  private boolean includeObsoleteEntities = false;
  
  @QueryParam("searchProperties")
  private JSONPObject searchProperties;
  
  @QueryParam("lang")
  private String lang = "en";
  
  @QueryParam("outputOpts")
  private JSONPObject outputOpts;
}
