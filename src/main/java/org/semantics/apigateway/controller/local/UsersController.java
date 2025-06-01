package org.semantics.apigateway.controller.local;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.semantics.apigateway.model.user.User;
import org.semantics.apigateway.service.auth.AuthService;
import org.semantics.apigateway.service.auth.UserRepository;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Tag(name = "Users")
@SecurityScheme(type = SecuritySchemeType.APIKEY)
@AllArgsConstructor
@CrossOrigin
public class UsersController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @GetMapping("/me")
    public User getCurrentUser() {
        return this.authService.getCurrentUser();
    }

    @GetMapping("/")
    public List<Map<String, String>> getAllUsers() {
        return this.userRepository.findAll().stream().map(x -> Map.of(
                "username", x.getUsername()
        )).toList();
    }
}
