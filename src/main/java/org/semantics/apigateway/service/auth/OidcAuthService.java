package org.semantics.apigateway.service.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.proc.DefaultJOSEObjectTypeVerifier;
import org.semantics.apigateway.model.user.TokenRequest;
import org.semantics.apigateway.model.user.TokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@Service
public class OidcAuthService {
  
  private final URI oidcTokenEndpointUri;
  
  @Value("${oidc.client-secret}")
  private String oidcClientSecret;
  
  // This is the default JWT decoder.
  // It can handle JWT tokens where the "typ" header parameter is missing
  // (as is the case in the NFDI InfraProxy's *id_tokens*),
  // but it cannot handle at+jwt tokens.
  private final JwtDecoder defaultJwtDecoder;
  
  // This is a JWTDecoder specifically configured to handle at+jwt tokens
  // (which is the object type of *access_tokens* returned by the NFDI InfraProxy).
  // Once a specific type verifier has been added, the decoder loses its
  // ability to decode JWT tokens without a "typ" header parameter.
  // So, unfortunately, we need the two separate JWTDecoders.
  private final JwtDecoder atJwtDecoder;
  
  private final HttpClient httpClient;
  private final ObjectMapper objectMapper = new ObjectMapper();
  
  public OidcAuthService(@Value("${oidc.token-endpoint}") String oidcTokenEndpoint, JwtDecoder defaultJwtDecoder, OAuth2ResourceServerProperties properties) throws URISyntaxException {
    oidcTokenEndpointUri = new URI(oidcTokenEndpoint);
    this.defaultJwtDecoder = defaultJwtDecoder;
    this.atJwtDecoder = NimbusJwtDecoder.withJwkSetUri(properties.getJwt().getJwkSetUri())
            .jwtProcessorCustomizer(customizer ->
                    customizer.setJWSTypeVerifier(new DefaultJOSEObjectTypeVerifier<>(
                            new JOSEObjectType("at+jwt"))))
            .build();
    httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
  }
  
  public CompletableFuture<TokenResponse> exchangeTokenForCode(TokenRequest tokenRequest) {
    HttpRequest request = HttpRequest.newBuilder()
            .uri(oidcTokenEndpointUri)
            .headers(
                    "Content-Type", "application/x-www-form-urlencoded",
                    "accept", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(getFormUrlEncodedBody(tokenRequest)))
            .build();
    return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(HttpResponse::body)
            .thenApply(body -> {
              try {
                return objectMapper.readValue(body, new TypeReference<>() {
                });
              } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to parse response from authorization server", e);
              }
            });
  }
  
  private String getFormUrlEncodedBody(TokenRequest tokenRequest) {
    return String.join("&", Stream.of(
            formUrlEncodedEntry("client_id", tokenRequest.getClient_id()),
            formUrlEncodedEntry("client_secret", oidcClientSecret),
            formUrlEncodedEntry("code", tokenRequest.getCode()),
            formUrlEncodedEntry("grant_type", tokenRequest.getGrant_type()),
            formUrlEncodedEntry("redirect_uri", tokenRequest.getRedirect_uri())
    ).flatMap(o -> o.isPresent() ? Stream.of(o.get()) : Stream.empty()).toList());
  }
  
  private static Optional<String> formUrlEncodedEntry(String key, String value) {
    if (value == null) {
      return Optional.empty();
    }
    
    StringBuilder b = new StringBuilder();
    b.append(URLEncoder.encode(key, StandardCharsets.UTF_8));
    b.append("=");
    b.append(URLEncoder.encode(value, StandardCharsets.UTF_8));
    return Optional.of(b.toString());
  }
  
  public Jwt verifyIdToken(String rawIdToken) {
    return defaultJwtDecoder.decode(rawIdToken);
  }
  
  public Jwt verifyAccessToken(String rawAccessToken) {
    return atJwtDecoder.decode(rawAccessToken);
  }
}
