package org.semantics.apigateway.service;

import org.semantics.apigateway.config.EndpointParameterMapping;
import org.semantics.apigateway.config.Pagination;

import java.util.Map;

public record UrlConfig(String url, String apikey, boolean caseInSensitive, Pagination pagination, Map<RequestParameter, EndpointParameterMapping> parameterMappings) {

}
