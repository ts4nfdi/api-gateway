package org.semantics.apigateway.service;

import org.semantics.apigateway.config.Pagination;

public record UrlConfig(String url, String apikey, boolean caseInSensitive, Pagination pagination) {

}
