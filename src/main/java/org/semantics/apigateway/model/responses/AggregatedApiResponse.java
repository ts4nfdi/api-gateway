package org.semantics.apigateway.model.responses;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@JsonSerialize(using = AggregatedApiResponse.CustomSerializer.class)
public class AggregatedApiResponse {
    private List<Map<String, Object>> collection = new ArrayList<>();
    @JsonIgnore
    private List<ApiResponse> originalResponses;
    private boolean showConfig = false;

    @JsonGetter
    public Map<String, Object> responseConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("totalResponseTime", totalResponseTime());

        config.put("databases", originalResponses.stream().map(x -> {
            Map<String, Object> response = new HashMap<>();
            response.put("url", x.getUrl());
            response.put("status", x.getStatusCode());
            response.put("responseTime", x.getResponseTime());
            return response;
        }));
        return config;
    }


    public static class CustomSerializer extends JsonSerializer<AggregatedApiResponse> {
        @Override
        public void serialize(AggregatedApiResponse response, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (!response.isShowConfig()) {
                gen.writeStartArray();
                for (Map<String, Object> item : response.getCollection()) {
                    gen.writeObject(item);
                }
                gen.writeEndArray();
            } else {
                gen.writeStartObject();
                gen.writeFieldName("collection");
                gen.writeObject(response.getCollection());
                if (response.responseConfig() != null) {
                    gen.writeFieldName("responseConfig");
                    gen.writeObject(response.responseConfig());
                }
                gen.writeEndObject();
            }
        }
    }

    public long totalResponseTime() {
        return originalResponses.stream().map(ApiResponse::getResponseTime).reduce(0L, Long::sum);
    }
}
