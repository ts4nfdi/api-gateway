package org.semantics.apigateway.controller;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.semantics.apigateway.model.responses.ErrorResponse;
import org.semantics.apigateway.model.user.InvalidJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.security.SignatureException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidJwtException.class)
    public ResponseEntity<ErrorResponse> handleJwtException(InvalidJwtException ex) {
        ErrorResponse errorResponse = new ErrorResponse("JWT_ERROR", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({ExpiredJwtException.class, MalformedJwtException.class, SignatureException.class})
    public ResponseEntity<ErrorResponse> handleJwtProcessingException(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse("JWT_ERROR", "Invalid or expired token: " + ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse("BAD_CREDENTIALS", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }
}
