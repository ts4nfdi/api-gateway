package org.semantics.apigateway.model.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String orcid;
    private String fullname;
    private String role;
    private Date expiration;

}
