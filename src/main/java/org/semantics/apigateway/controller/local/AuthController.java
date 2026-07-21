package org.semantics.apigateway.controller.local;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.semantics.apigateway.model.responses.SuccessResponse;
import org.semantics.apigateway.model.user.*;
import org.semantics.apigateway.service.auth.AuthService;
import org.semantics.apigateway.service.auth.JwtUtil;
import org.semantics.apigateway.service.auth.OidcAuthService;
import org.semantics.apigateway.service.auth.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

@CrossOrigin
@RestController
@RequestMapping("/auth")
@Tag(name = "Users - Authentication")
@RequiredArgsConstructor
public class AuthController {
  
  private final AuthenticationManager authenticationManager;
  private final JwtUtil jwtUtil;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthService authService;
  private final OidcAuthService oidcAuthService;
  
  @Value("${oidc.client-id}")
  private final String clientId;
  @Value("${oidc.client-secret}")
  private final String clientSecret;
  @Value("${oidc.authorization-endpoint}")
  private final String authorizationEndpoint;
  
  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public SuccessResponse registerUser(@Valid @RequestBody RegisterRequest user) {
    User newUser = new User();
    newUser.setUsername(user.getUsername());
    newUser.setOidcSubjectIdentifier(user.getOidcSubjectIdentifier());
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
  
  @GetMapping("/sso/authorize")
  public RedirectView getAuthentication(@RequestParam String redirect_uri, @RequestParam(required = false) String state) {
    
    RedirectView response = new RedirectView(authorizationEndpoint);
    response.addStaticAttribute("response_type", "code");
    response.addStaticAttribute("scope", "openid email profile");
    response.addStaticAttribute("client_id", clientId);
    response.addStaticAttribute("redirect_uri", redirect_uri);
    if (state != null)
      response.addStaticAttribute("state", state);
    return response;
  }
  
  @PostMapping(path = "/sso/token", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.APPLICATION_JSON_VALUE})
  public @ResponseBody CompletableFuture<TokenResponse> getToken(@RequestBody TokenRequest tokenRequest) {
    tokenRequest.setClient_id(clientId);
    tokenRequest.setClient_secret(clientSecret);
    tokenRequest.setGrant_type("authorization_code");
    return oidcAuthService.exchangeTokenForCode(tokenRequest);
  }
  
  @PostMapping("/sso/login")
  public AuthResponse loginSsoUser(@RequestBody SsoLoginRequest loginRequest, @RequestParam(required = false) String redirect_url) {
    Jwt verifiedIdToken = oidcAuthService.verifyIdToken(loginRequest.getId_token());
    String subject = verifiedIdToken.getSubject();
    
    Jwt verifiedAccessToken = oidcAuthService.verifyAccessToken(loginRequest.getAccess_token());
    
    // TODO add orcid claim as soon as supported by IDP
    Object orcidClaim = verifiedIdToken.getClaim("orcid");
    
    if (verifiedIdToken.getExpiresAt().isBefore(Instant.now())) {
      throw new OAuth2AuthenticationException("Token expired");
    }
    
    if (!verifiedIdToken.getAudience().get(0).equals(clientId)) {
      throw new OAuth2AuthenticationException("Invalid audience");
    }

    User user = userRepository.findByOidcSubjectIdentifier(subject).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    UserDetails userDetails = authService.loadUserByUsername(user.getUsername());
    
    String token = jwtUtil.generateToken(userDetails.getUsername());
    Date expiration = jwtUtil.extractExpiration(token);
    
    GrantedAuthority role = userDetails.getAuthorities().stream().findFirst().orElse(null);
    return new AuthResponse(token, user.getUsername(), role != null ? role.getAuthority() : "", expiration);
  }
}
