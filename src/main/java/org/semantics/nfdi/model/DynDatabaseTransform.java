package org.semantics.nfdi.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.semantics.nfdi.config.MappingConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DynDatabaseTransform {

    private final MappingConfig mappingConfig;

    @Autowired
    public DynDatabaseTransform(MappingConfig mappingConfig) {
        this.mappingConfig = mappingConfig;
    }

    public Map<String, Object> transformDatabaseResponse(List<Map<String, Object>> olsResponse) {
        List<Map<String, Object>> transformedDocs = new ArrayList<>();
        Map<String, String> mapping = mappingConfig.getMapping();
        Map<String, Object> responseStructure = mappingConfig.getResponseStructure();

        for (Map<String, Object> item : olsResponse) {
            Map<String, Object> transformedItem = new HashMap<>();
            for (Map.Entry<String, String> entry : mapping.entrySet()) {
                String olsField = entry.getKey();
                String responseField = entry.getValue();
                transformedItem.put(olsField, item.get(responseField));
            }
            transformedDocs.add(transformedItem);
        }

        Map<String, Object> response = new HashMap<>();
        response.put((String) responseStructure.get("docs"), transformedDocs);
        response.put(responseStructure.get("numFound").toString(), transformedDocs.size());
        response.put(responseStructure.get("start").toString(), 0);

        Map<String, Object> wrappedResponse = new HashMap<>();
        wrappedResponse.put("response", response);
        wrappedResponse.put("responseHeader", createResponseHeader(responseStructure));

        return wrappedResponse;
    }

    private Map<String, Object> createResponseHeader(Map<String, Object> responseStructure) {
        Map<String, Object> responseHeader = new HashMap<>();
        Map<String, String> headerStructure = (Map<String, String>) responseStructure.get("responseHeader");
        responseHeader.put(headerStructure.get("QTime"), 0); // Placeholder for query time
        responseHeader.put(headerStructure.get("status"), 0); // Placeholder for status
        return responseHeader;
    }
}
