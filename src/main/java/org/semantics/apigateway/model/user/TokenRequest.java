package org.semantics.apigateway.model.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenRequest {
    private String code;
    private String client_id;
    private String client_secret;
    private String grant_type;
    private String redirect_uri;
    private String code_verifier;
}
