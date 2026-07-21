package org.semantics.apigateway.model.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SsoLoginRequest {
  private String access_token;
  private String id_token;
}
