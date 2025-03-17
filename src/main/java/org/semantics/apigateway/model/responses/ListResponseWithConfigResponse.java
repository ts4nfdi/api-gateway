package org.semantics.apigateway.model.responses;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ListResponseWithConfigResponse  {
    private final List<Map<String, Object>> collection;
    private final ResponseConfig responseConfig;

    public ListResponseWithConfigResponse(AggregatedApiResponse source) {
        this.collection = source.getCollection();
        this.responseConfig = source.responseConfig();
    }
}
