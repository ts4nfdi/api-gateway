package org.semantics.apigateway.model.responses;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public  class ApiResponse {
    private Map<String, Object> responseBody = new HashMap<>();
    private int statusCode;
    private long responseTime;
    private String url;
    private String name;
}
