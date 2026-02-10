package org.semantics.apigateway.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Pagination {
  
  public enum PaginationType {page, offset, none}
  
  private PaginationType type = PaginationType.none;
  private int first = 0;
}
