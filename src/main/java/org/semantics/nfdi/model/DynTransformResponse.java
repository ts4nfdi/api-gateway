package org.semantics.nfdi.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.semantics.nfdi.config.OntologyConfig;
import org.semantics.nfdi.config.ResponseMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.apache.commons.beanutils.PropertyUtils;

@Component
public class DynTransformResponse {

    private static final Logger logger = LoggerFactory.getLogger(DynTransformResponse.class);

    public List<Map<String, Object>> dynTransformResponse(Map<String, Object> response, OntologyConfig config) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (response == null) {
            logger.error("Response is null");
            return result;
        }

        String nestedJsonKey = config.getResponseMapping().getNestedJson();
        Object nestedData = response.getOrDefault(nestedJsonKey, new ArrayList<>());
        logger.info("Nested JSON key: {}", nestedJsonKey);
        logger.info("Nested data type: {}", nestedData.getClass().getSimpleName());

        if (nestedData instanceof List) {
            processList((List<?>) nestedData, result, config);
        } else if (nestedData instanceof Map) {
            Object docs = ((Map<?, ?>) nestedData).get("docs");
            if (docs instanceof List) {
                processList((List<?>) docs, result, config);
            } else {
                logger.error("Expected List for key 'docs', but found: {}", docs.getClass().getSimpleName());
            }
        } else {
            logger.error("Expected List or Map for nested JSON key: {}, but found: {}", nestedJsonKey, nestedData.getClass().getSimpleName());
        }

        logger.info("Transformed response: {}", result);
        return result;
    }

    private void processList(List<?> dataList, List<Map<String, Object>> result, OntologyConfig config) {
        for (Object item : dataList) {
            if (item instanceof Map) {
                Map<String, Object> newItem = processItem((Map<String, Object>) item, config);
                if (!newItem.isEmpty()) {
                    result.add(newItem);
                }
            }
        }
    }

    private Map<String, Object> processItem(Map<String, Object> item, OntologyConfig config) {
        Map<String, Object> newItem = new HashMap<>();
        if (item == null) {
            logger.error("Item is null");
            return newItem;
        }

        ResponseMapping responseMapping = config.getResponseMapping(); 
        List<String> fieldList = responseMapping.getFieldList(); 

        for (String field : fieldList) {
            try {
                Object value = PropertyUtils.getNestedProperty(item, field);
                if (value != null) {
                    newItem.put(field, value);
                    logger.info("Accessed field: {} with value: {}", field, value);
                } else {
                    logger.warn("Value for field {} is null or missing", field);
                }
            } catch (Exception e) {
                logger.error("Error accessing or mapping field: {}", field, e);
            }
        }

        return newItem;
    }
}
