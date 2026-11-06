package org.semantics.apigateway.controller;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class TrailingSlashHandlerFilter extends OncePerRequestFilter {
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    String requestUri = request.getRequestURI();
    
    if (requestUri.endsWith("/") && !requestUri.equals(request.getContextPath() + "/")) {
      String newUrl = requestUri.substring(0, requestUri.length() - 1);
      if (!request.getParameterMap().isEmpty()) {
        newUrl += "?"
                + request.getParameterMap().entrySet().stream().map(e -> e.getKey() + "="
                + Arrays.stream(e.getValue()).collect(Collectors.joining(",")))
                .collect(Collectors.joining("&"));
      }
      response.setStatus(HttpStatus.MOVED_PERMANENTLY.value());
      response.setHeader(HttpHeaders.LOCATION, newUrl);
      return;
    }
    
    filterChain.doFilter(request, response);
  }
}
