package org.semantics.nfdi.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.semantics.nfdi.config.OntologyConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component
public class DynTransformResponse {

    private static final Logger logger = LoggerFactory.getLogger(DynTransformResponse.class);

    public List<Map<String, Object>> dynTransformResponse(Map<String, Object> response, OntologyConfig config) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (response == null) {
            return result;
        }

        // Assume 'docs' is the common key for entries in the response of different databases
        List<Object> docs = (List<Object>) response.getOrDefault("docs", new ArrayList<>());

        logger.info("Response: {}", response);

        for (Object doc : docs) {
            if (doc == null) {
                continue;
            }

            Map<String, Object> docMap = (Map<String, Object>) doc;
            Map<String, Object> newItem = new HashMap<>();

            // Use the fields from OntologyConfig to process the response
            for (String field : config.getFields()) {
                try {
                    Object value = PropertyUtils.getNestedProperty(docMap, field);
                    newItem.put(field, value);
                } catch (Exception e) {
                    logger.error("Error mapping response field: {}", field, e);
                }
            }

            result.add(newItem);
        }

        logger.info("Transformed response: {}", result);

        return result;
    }
}
