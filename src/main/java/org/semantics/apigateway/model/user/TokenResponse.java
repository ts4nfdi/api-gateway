package org.semantics.apigateway.model.user;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenResponse {
  
  @NotEmpty
  private String access_token;
  
  @NotEmpty
  private String scope;
  
  @NotEmpty
  private String id_token;
  
  @NotEmpty
  private String token_type;
  
  @NotEmpty
  private long expires_in;
}
