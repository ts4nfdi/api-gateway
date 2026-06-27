package org.semantics.apigateway.service.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.semantics.apigateway.model.user.TokenRequest;
import org.semantics.apigateway.model.user.TokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

@Service
public class TokenExchangeService {
  
  private final URI oidcTokenEndpointUri;
  
  @Value("${oidc.client.secret}")
  private String oidcClientSecret;
  
  private final HttpClient httpClient;
  private final ObjectMapper objectMapper = new ObjectMapper();
  
  public TokenExchangeService(@Value("${oidc.server.token-endpoint}") String oidcTokenEndpoint) throws URISyntaxException {
    oidcTokenEndpointUri = new URI(oidcTokenEndpoint);
    httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
  }
  
  public CompletableFuture<TokenResponse> exchangeTokenForCode(TokenRequest tokenRequest) {
    HttpRequest request = HttpRequest.newBuilder()
            .uri(oidcTokenEndpointUri)
            .headers(
                    "Content-Type",  "application/x-www-form-urlencoded",
                    "accept",  "application/json")
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
    StringBuilder formBodyBuilder = new StringBuilder();
    
    appendFormUrlEncodedEntry(formBodyBuilder, "client_id", tokenRequest.getClient_id());
    formBodyBuilder.append("&");
    appendFormUrlEncodedEntry(formBodyBuilder, "client_secret", oidcClientSecret);
    formBodyBuilder.append("&");
    appendFormUrlEncodedEntry(formBodyBuilder, "code", tokenRequest.getCode());
    formBodyBuilder.append("&");
    appendFormUrlEncodedEntry(formBodyBuilder, "code_verifier", tokenRequest.getCode_verifier());
    formBodyBuilder.append("&");
    appendFormUrlEncodedEntry(formBodyBuilder, "grant_type", tokenRequest.getGrant_type());
    formBodyBuilder.append("&");
    appendFormUrlEncodedEntry(formBodyBuilder, "redirect_uri", tokenRequest.getRedirect_uri());

    return formBodyBuilder.toString();
  }
  
  private static void appendFormUrlEncodedEntry(StringBuilder formBodyBuilder, String key, String value) {
    if (value != null) {
      formBodyBuilder.append(URLEncoder.encode(key, StandardCharsets.UTF_8));
      formBodyBuilder.append("=");
      formBodyBuilder.append(URLEncoder.encode(value, StandardCharsets.UTF_8));
    }
  }
}
