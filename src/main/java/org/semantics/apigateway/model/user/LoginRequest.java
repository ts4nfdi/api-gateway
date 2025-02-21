package org.semantics.apigateway.model.user;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
public class LoginRequest {

    @NotNull
    private String username;

    @NotNull
    private String password;
}
