package org.semantics.apigateway.controller.local;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@Tag(name = "Authentication")
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
  @Operation(summary = "Register a new user account via single sign-on.",
          description = "Creates a new user account linked to an NFDI AAI single sign-on (SSO) identity. " +
                  "Send the chosen `username` and the `id_token` received from the SSO flow (see `/auth/sso/authorize` and `/auth/sso/token`). " +
                  "An ORCID iD is required: it is taken from the ID token, or from the `orcid` field if the token contains none. " +
                  "After registering, log in with `POST /auth/sso/login`.")
  public SuccessResponse registerUser(@Valid @RequestBody RegisterRequest user) {
    Jwt idTokenVerified = oidcAuthService.verifyIdToken(user.getId_token());
    String orcid = idTokenVerified.getClaim("orcid");
    if (orcid == null) {
      orcid = user.getOrcid();
    }
    if (orcid == null) {
      throw new MissingOrcidException("No orcid in id token or request parameters");
    }
    
    User newUser = new User();
    newUser.setUsername(user.getUsername());
    newUser.setOidcSubjectIdentifier(idTokenVerified.getSubject());
    newUser.setPrimaryOrcid(orcid);
    newUser.getExternalPersistentIdentifiers().add(orcid);
    
    newUser.setRoles(Collections.singleton(Role.USER));
    userRepository.save(newUser);
    
    return new SuccessResponse(
            "User created successfully",
            "success");
  }
  
  @PostMapping("/login")
  @Operation(summary = "Log in with username and password.",
          description = "Checks `username` and `password` and returns a bearer token (JWT) together with the username, role, and expiration date of the token. " +
                  "Send the token in the `Authorization: Bearer <token>` header (or use the *Authorize* button) to call endpoints that require login. " +
                  "Accounts created via single sign-on log in with `POST /auth/sso/login` instead.")
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
  @Operation(summary = "Log out.",
          description = "Ends the current request's login on the gateway. " +
                  "The bearer token itself stays valid until it expires, so clients should also delete the token on their side.")
  public SuccessResponse logout(HttpServletRequest request) {
    SecurityContextHolder.clearContext();
    return new SuccessResponse("Logged out successfully.", "success");
  }

  @GetMapping("/me")
  @Operation(summary = "Get your own user profile.",
          description = "Returns the profile of the logged-in user; the same as `GET /users/me`. Useful to check whether a bearer token is valid. Requires login.")
  public User getCurrentUser() {
    return this.authService.getCurrentUser();
  }

  @GetMapping("/sso/authorize")
  @Operation(summary = "Start single sign-on (step 1 of 3).",
          description = "Redirects the browser to the login page of NFDI AAI (OpenID Connect). " +
                  "After a successful login there, the browser is sent back to `redirect_uri` with an authorization `code` (and the given `state`). " +
                  "Continue with `POST /auth/sso/token`.")
  public RedirectView getAuthentication(@Parameter(description = "URL of your application to which NFDI AAI redirects after login") @RequestParam String redirect_uri,
                                        @Parameter(description = "Optional value that is passed back unchanged to `redirect_uri`, e.g. to protect against CSRF") @RequestParam(required = false) String state) {
    
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
  @Operation(summary = "Exchange the SSO authorization code for tokens (step 2 of 3).",
          description = "Exchanges the authorization `code` received at `redirect_uri` (see `/auth/sso/authorize`) for an ID token and an access token from NFDI AAI. " +
                  "Send the same `redirect_uri` as in step 1. Continue with `POST /auth/sso/login` (or `POST /auth/register` for new users).")
  public @ResponseBody CompletableFuture<TokenResponse> getToken(@RequestBody TokenRequest tokenRequest) {
    tokenRequest.setClient_id(clientId);
    tokenRequest.setClient_secret(clientSecret);
    tokenRequest.setGrant_type("authorization_code");
    return oidcAuthService.exchangeTokenForCode(tokenRequest);
  }
  
  @PostMapping("/sso/login")
  @Operation(summary = "Log in with single sign-on tokens (step 3 of 3).",
          description = "Checks the `id_token` and `access_token` received from `POST /auth/sso/token` and returns a bearer token (JWT) of the gateway, " +
                  "together with the username, role, and expiration date. The user must have registered before (see `POST /auth/register`).")
  public AuthResponse loginSsoUser(@RequestBody SsoLoginRequest loginRequest) {
    Jwt verifiedIdToken = oidcAuthService.verifyIdToken(loginRequest.getId_token());
    String subject = verifiedIdToken.getSubject();
    
    Jwt verifiedAccessToken = oidcAuthService.verifyAccessToken(loginRequest.getAccess_token());
    
    if (verifiedIdToken.getExpiresAt().isBefore(Instant.now())) {
      throw new OAuth2AuthenticationException("Token expired");
    }
    
    if (!verifiedIdToken.getAudience().get(0).equals(clientId)) {
      throw new OAuth2AuthenticationException("Invalid audience");
    }

    User user = userRepository.findByOidcSubjectIdentifier(subject).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
            .username(user.getUsername())
            .password("")
            .roles(user.getRoles().stream().map(Enum::toString).toArray(String[]::new))
            .build();
    
    String token = jwtUtil.generateToken(userDetails.getUsername());
    Date expiration = jwtUtil.extractExpiration(token);
    
    GrantedAuthority role = userDetails.getAuthorities().stream().findFirst().orElse(null);
    return new AuthResponse(token, user.getUsername(), role != null ? role.getAuthority() : "", expiration);
  }
}
