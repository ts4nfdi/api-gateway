package org.semantics.apigateway.model.responses;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.semantics.apigateway.collections.models.TerminologyCollection;
import org.semantics.apigateway.service.StatusService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@JsonSerialize(using = AggregatedApiResponse.CustomSerializer.class)
public class AggregatedApiResponse {
    private List<Map<String, Object>> collection = new ArrayList<>();
    private List<ApiResponse> originalResponses;
    private boolean showConfig = false;
    private String endpoint = null;
    private boolean isList = true;
    private boolean paginate = false;
    private long totalCount = 0;
    private int page = 0;
    private TerminologyCollection terminologyCollection = null;

    @JsonGetter
    public ResponseConfig responseConfig() {
        ResponseConfig config = new ResponseConfig();
        config.setEndpoint(endpoint);
        config.setTotalResponseTime(totalResponseTime());
        config.setTotalResults(collection.size());

        config.setOriginalResponses(originalResponses.stream().map(x -> {
            ResponseConfig.OriginalResponseStats response = new ResponseConfig.OriginalResponseStats();
            response.setUrl(x.getUrl());
            response.setStatusCode(x.getStatusCode());
            response.setResponseTime(x.getResponseTime());
            return response;
        }).toList());

        if (terminologyCollection != null) {
            config.setCollection(terminologyCollection);
        }

        for (Map<String, Object> item : collection) {
            config.getResults().add(calculateStats(item));
        }

        config.setAvgPercentageCommon(config.getResults().stream().mapToDouble(StatusService.StatusResult::getPercentageCommon).average().orElse(0.0));
        config.setAvgPercentageFilled(config.getResults().stream().mapToDouble(StatusService.StatusResult::getPercentageFilled).average().orElse(0.0));

        return config;
    }

    public void setPaginate(boolean paginate) {
        this.paginate = paginate;
        this.setList(!paginate);
    }





    public static class CustomSerializer extends JsonSerializer<AggregatedApiResponse> {
        @Override
        public void serialize(AggregatedApiResponse response, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            Object output;

            if (response.isPaginate()) {
                if (response.isShowConfig()) {
                    output = new PaginatedWithConfigResponse(response);
                } else {
                    output = new PaginatedResponse(response);
                }
            } else if (response.isShowConfig()) {
                output = new ListResponseWithConfigResponse(response);
            } else {
                if (!response.isList() && !response.getCollection().isEmpty()) {
                    // Return just the first item for a single object response
                    output = response.getCollection().get(0);
                } else {
                    // Return the collection directly for a list response
                    output = response.getCollection();
                }
            }

            gen.writeObject(output);
        }
    }

    public long totalResponseTime() {
        return originalResponses.stream().map(ApiResponse::getResponseTime).reduce(0L, Long::sum);
    }


    private static StatusService.StatusResult calculateStats(Map<String, Object> data) {
        StatusService.StatusResult result = new StatusService.StatusResult();
        List<String> mainDataKeys = new ArrayList<>();
        for (String key : data.keySet()) {
            if (!key.equals("originalResponse")) {
                mainDataKeys.add(key);
            }
        }

        Map<String, Object> originalResponse = (Map<String, Object>) data.get("originalResponse");
        List<String> commonKeys = new ArrayList<>();
        for (String key : originalResponse.keySet()) {
            if (mainDataKeys.contains(key)) {
                commonKeys.add(key);
            }
        }

        List<String> emptyKeys = new ArrayList<>();
        for (String key : mainDataKeys) {
            Object value = data.get(key);
            boolean isEmpty = value == null ||
                    value.equals("") ||
                    (value instanceof List && ((List<?>) value).isEmpty());
            if (isEmpty) {
                emptyKeys.add(key);
            }
        }

        double percentageFilled = (1 - ((double) emptyKeys.size() / mainDataKeys.size())) * 100;
        double percentageCommon = ((double) commonKeys.size() / originalResponse.size()) * 100;
        result.setPercentageCommon(percentageCommon);
        result.setPercentageFilled(percentageFilled);
        result.setCommonKeys(commonKeys);
        result.setEmptyKeys(emptyKeys);
        result.setTotalMainKeys(mainDataKeys.size());
        result.setTotalOriginalKeys(originalResponse.size());

        return result;
    }
}
