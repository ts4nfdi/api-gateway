package org.semantics.apigateway.model.user;

import lombok.Getter;

@Getter
public class InvalidJwtException extends RuntimeException {
    public InvalidJwtException(String message) {
        super(message);
    }
}
