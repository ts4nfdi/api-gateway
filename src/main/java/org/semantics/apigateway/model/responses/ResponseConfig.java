package org.semantics.apigateway.model.responses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.semantics.apigateway.model.user.TerminologyCollection;
import org.semantics.apigateway.service.StatusService;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ResponseConfig {
    private double totalResponseTime;
    private int totalResults;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String endpoint = null;
    private double AvgPercentageCommon = 0.0;
    private double AvgPercentageFilled = 0.0;

    @JsonProperty("databases")
    private List<OriginalResponseStats> originalResponses;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TerminologyCollection collection;


    @JsonIgnore
    private List<StatusService.StatusResult> results = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OriginalResponseStats {
        private String url;
        private int statusCode;
        private double responseTime;
    }
}
