package org.semantics.apigateway.controller;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TrailingSlashHandlerFilter extends OncePerRequestFilter {
  @Override
  protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
    
    String requestUri = request.getRequestURI();
    
    if (requestUri.endsWith("/") && !requestUri.equals(request.getContextPath() + "/")) {
      HttpServletRequestWrapper wrapper =
              new HttpServletRequestWrapper(request) {
                
                @Override
                public String getRequestURI() {
                  return removeTrailingSlash(super.getRequestURI());
                }
                
                @Override
                public StringBuffer getRequestURL() {
                  return new StringBuffer(
                          removeTrailingSlash(super.getRequestURL().toString()));
                }
                
                @Override
                public String getServletPath() {
                  return removeTrailingSlash(super.getServletPath());
                }
                
                @Override
                public String getPathInfo() {
                  return removeTrailingSlash(super.getPathInfo());
                }
              };
      
      filterChain.doFilter(wrapper, response);
      return;
    }
    
    filterChain.doFilter(request, response);
  }
  
  private static String removeTrailingSlash(String path) {
    if (path != null && path.length() > 1 && path.endsWith("/")) {
      return path.substring(0, path.length() - 1);
    }
    return path;
  }
}
