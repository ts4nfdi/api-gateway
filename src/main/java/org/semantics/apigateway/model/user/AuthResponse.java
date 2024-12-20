package org.semantics.apigateway.model.user;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class AuthResponse {

    private String token;
    private String username;
    private String role;
    private Date expiration;

    public AuthResponse(String token, String username, String role, Date expiration) {
        this.token = token;
        this.username = username;
        this.role = role;
        this.expiration = expiration;
    }

}
