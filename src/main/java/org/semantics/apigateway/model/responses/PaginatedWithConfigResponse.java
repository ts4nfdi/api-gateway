package org.semantics.apigateway.model.responses;

import lombok.Getter;


@Getter
public class PaginatedWithConfigResponse extends PaginatedResponse {
    private final ResponseConfig responseConfig;

    public PaginatedWithConfigResponse(AggregatedApiResponse source) {
        super(source);
        this.responseConfig = source.responseConfig();
    }
}
