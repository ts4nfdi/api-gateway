package org.semantics.apigateway.controller.local;

public class MissingOrcidException extends RuntimeException {
  public MissingOrcidException(String message) {
    super(message);
  }
}
