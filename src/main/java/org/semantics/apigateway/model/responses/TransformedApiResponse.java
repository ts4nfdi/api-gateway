package org.semantics.apigateway.model.responses;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public  class TransformedApiResponse {
    private List<AggregatedResourceBody> collection = new ArrayList<>();
    private ApiResponse originalResponse;

    public List<Map<String, Object>> getCollection() {
        return collection.stream().map(AggregatedResourceBody::toMap).collect(Collectors.toList());
    }
}
