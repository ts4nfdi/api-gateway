package org.semantics.apigateway.controller.ols.model;

import lombok.Data;

@Data
public class Pageable {
  private int page = 0;
  private int size = 20;
}
