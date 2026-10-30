package org.semantics.apigateway.controller.local;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    @Operation(summary = "Get your own user profile.",
            description = "Returns the profile of the logged-in user, e.g. username, roles, primary ORCID iD, and external identifiers. Requires login.")
    public User getCurrentUser() {
        return this.authService.getCurrentUser();
    }

    @GetMapping("/")
    @Operation(summary = "List all usernames.",
            description = "Returns the usernames of all registered users, e.g. to pick collaborators for a terminology collection. Requires login.")
    public List<Map<String, String>> getAllUsers() {
        getCurrentUser();
        return this.userRepository.findAll().stream().map(x -> Map.of(
                "username", x.getUsername()
        )).toList();
    }
    
    @PostMapping("/me/externalIdentifiers")
    @Operation(summary = "Add an external identifier to your profile.",
            description = "Adds a persistent identifier of the logged-in user from another system, e.g. an ORCID iD, to the user's profile. " +
                    "Send the identifier as plain text in the request body. Requires login.")
    public void updateUser(@RequestBody String externalIdentifier) {
        User user = getCurrentUser();
        user.getExternalPersistentIdentifiers().add(externalIdentifier);
        userRepository.save(user);
    }

    @GetMapping("/{externalIdentifier}")
    @Operation(summary = "Find a user by an external identifier.",
            description = "Returns the user who has the given external persistent identifier (e.g. an ORCID iD) in their profile, or an empty result if there is none.")
    public Optional<User> getUserByExternalId(@Parameter(description = "External persistent identifier, e.g. an ORCID iD", example = "0000-0002-1825-0097")
                                              @PathVariable("externalIdentifier") String externalIdentifier) {
        return userRepository.findByExternalPersistentIdentifiers(externalIdentifier);
    }
    
}
