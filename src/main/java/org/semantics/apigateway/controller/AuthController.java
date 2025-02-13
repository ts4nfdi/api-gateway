package org.semantics.apigateway.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.semantics.apigateway.model.responses.SuccessResponse;
import org.semantics.apigateway.model.user.*;
import org.semantics.apigateway.service.auth.AuthService;
import org.semantics.apigateway.service.auth.JwtUtil;
import org.semantics.apigateway.service.auth.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Date;

@RestController
@RequestMapping("/auth")
@Tag(name = "Users - Authentification")
@AllArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;


    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public SuccessResponse registerUser(@Valid @RequestBody RegisterRequest user) {
        User newUser = new User();
        newUser.setUsername(user.getUsername());
        newUser.setPassword(passwordEncoder.encode(user.getPassword()));
        newUser.setRoles(Collections.singleton(Role.USER));
        userRepository.save(newUser);

        return new SuccessResponse(
                "User created successfully",
                "success");
    }

    @PostMapping("/login")
    public AuthResponse loginUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        String token = jwtUtil.generateToken(authentication.getName());
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();


        Date expiration = jwtUtil.extractExpiration(token);
        GrantedAuthority role = userDetails.getAuthorities().stream().findFirst().orElse(null);
        return new AuthResponse(token, loginRequest.getUsername(), role != null ? role.getAuthority() : "", expiration);

    }

    @GetMapping("/logout")
    public SuccessResponse logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        return new SuccessResponse("Logged out successfully.", "success");
    }

    @GetMapping("/me")
    public User getCurrentUser() {
        return this.authService.getCurrentUser();
    }
}
