package org.semantics.apigateway.model.responses;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public  class TransformedApiResponse<T extends AggregatedResourceBody> {
    private List<T> collection = new ArrayList<>();
    private long totalCollections;
    private int page;
    private boolean paginate;
    private ApiResponse originalResponse;

    public List<Map<String, Object>> getCollection(boolean showOriginalResponse) {
        return collection.stream().map(x -> x.toMap(showOriginalResponse, true)).toList();
    }
}
