package org.semantics.apigateway.model.user;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenRequest {
    @NotEmpty
    private String grant_type;
    
    @NotEmpty
    private String redirect_uri;
    
    @NotEmpty
    private String code;
    
    @NotEmpty
    private String code_verifier;
    
    private String client_id;
    private String client_secret;
    
}
