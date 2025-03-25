package org.semantics.apigateway.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JsonLdTransform {

    public List<Map<String, Object>> convertToJsonLd(List<Map<String, Object>> response, String type) {
        Map<String, Object> context = new HashMap<>();
        context.put("@vocab", "http://base4nfdi.de/ts4nfdi/schema/");
        context.put("ts", "http://base4nfdi.de/ts4nfdi/schema/");

        List<Map<String, Object>> nestedData = response;

        nestedData = nestedData.stream().peek(item -> {
            item.put("@context", context);
            item.put("@type", type);
            item.put("@id", item.get("iri"));
        }).collect(Collectors.toList());

        return nestedData;
    }
}
