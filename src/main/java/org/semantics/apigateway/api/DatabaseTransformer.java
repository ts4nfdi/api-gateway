package org.semantics.apigateway.api;

import java.util.List;
import java.util.Map;

public interface DatabaseTransformer {
    Map<String, Object> transformItem(Map<String, Object> item);
    Map<String, Object> constructResponse(List<Map<String, Object>> transformedResults);

    // You can incorporate a new transformer class into the application and then restart it to establish a
    // mapping to database schema of your preference
}
