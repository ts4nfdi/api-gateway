package org.semantics.apigateway.api;

import org.semantics.apigateway.config.ResponseMapping;
import org.semantics.apigateway.model.SemanticArtefact;
import org.semantics.apigateway.service.MappingTransformer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OlsTransformer implements TargetSchemaTransformer {
    @Override
    public Map<String, Object> transformItem(Map<String, Object> item, ResponseMapping mapping) {
        if (item == null) {
            return null;
        }

        Map<String, Object> transformedItem = new HashMap<>();
        if (mapping == null) {
            return item;
        }

        mapping.inverseMapping().forEach((transformedKey, ourKey) -> {

            Object value = item.get(ourKey);
            if (value == null) {
                value = item.get(toSnakeCaseRegex(ourKey));
            }

            MappingTransformer.itemValueSetter(transformedItem, transformedKey, value);
        });

        transformedItem.put("URI", item.get("iri"));
        transformedItem.put("@type", new SemanticArtefact().getTypeURI());
        return transformedItem;
    }


    @Override
    public Map<String, Object> constructResponse(List<Map<String, Object>> transformedResults, boolean list) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> innerResponse = new HashMap<>();
        List<Map<String, Object>> docs = new ArrayList<>();

        // Construct the "docs" list
        for (Map<String, Object> transformedResult : transformedResults) {
            Map<String, Object> doc = new HashMap<>();
            doc.putAll(transformedResult);
            docs.add(doc);
        }

        innerResponse.put("docs", docs);
        innerResponse.put("numFound", transformedResults.size());
        innerResponse.put("start", 0);

        response.put("response", innerResponse);

        Map<String, Object> responseHeader = new HashMap<>();
        responseHeader.put("QTime", 0);
        responseHeader.put("status", 0);

        response.put("responseHeader", responseHeader);

        return response;
    }
}
