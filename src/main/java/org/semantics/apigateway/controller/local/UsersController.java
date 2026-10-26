package org.semantics.apigateway.controller.local;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.semantics.apigateway.model.user.User;
import org.semantics.apigateway.service.auth.AuthService;
import org.semantics.apigateway.service.auth.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        getCurrentUser();
        return this.userRepository.findAll().stream().map(x -> Map.of(
                "username", x.getUsername(),
                "orcid", Optional.ofNullable(x.getPrimaryOrcid()).orElse("")
        )).toList();
    }
    
    @PostMapping("/me/externalIdentifiers")
    public void updateUser(@RequestBody String externalIdentifier) {
        User user = getCurrentUser();
        user.getExternalPersistentIdentifiers().add(externalIdentifier);
        userRepository.save(user);
    }
    
    @GetMapping("/{externalIdentifier}")
    public Optional<User> getUserByExternalId(@PathVariable("externalIdentifier") String externalIdentifier) {
        return userRepository.findByExternalPersistentIdentifiers(externalIdentifier);
    }
    
}
