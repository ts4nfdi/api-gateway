package org.semantics.apigateway.api;

import org.semantics.apigateway.config.ResponseMapping;

import java.util.List;
import java.util.Map;

public interface DatabaseTransformer {
    Map<String, Object> transformItem(Map<String,Object> item, ResponseMapping mapping);
    Map<String, Object> constructResponse(List<Map<String, Object>> transformedResults, boolean list);

    // You can incorporate a new transformer class into the application and then restart it to establish a
    // mapping to database schema of your preference
}
