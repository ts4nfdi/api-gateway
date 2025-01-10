package org.semantics.apigateway.model.responses;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SuccessResponse {
    private String message;
    private String status;
}
