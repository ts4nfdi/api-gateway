package org.semantics.apigateway.controller;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.semantics.apigateway.model.user.User;
import org.semantics.apigateway.service.auth.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@Tag(name = "Users")
@SecurityScheme(type = SecuritySchemeType.APIKEY)
@AllArgsConstructor
public class UsersController {

    private final AuthService authService;

    @GetMapping("/me")
    public User getCurrentUser() {
        return this.authService.getCurrentUser();
    }
}
