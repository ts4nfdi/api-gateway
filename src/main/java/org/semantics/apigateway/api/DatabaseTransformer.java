package org.semantics.apigateway.api;

import org.semantics.apigateway.config.ResponseMapping;

import java.util.List;
import java.util.Map;

public interface DatabaseTransformer {

    default String toSnakeCaseRegex(String camelCase) {
        if (camelCase == null || camelCase.isEmpty()) {
            return camelCase;
        }

        // Replace uppercase letters preceded by a lowercase letter with '_' + lowercase

        return camelCase.replaceAll(
                "(?<=[a-z0-9])(?=[A-Z])",
                "_"
        ).toLowerCase();
    }

    Map<String, Object> transformItem(Map<String,Object> item, ResponseMapping mapping);
    Map<String, Object> constructResponse(List<Map<String, Object>> transformedResults, String mappingKey, boolean list, boolean paginate, int page, long totalCount);

    // You can incorporate a new transformer class into the application and then restart it to establish a
    // mapping to database schema of your preference
}
