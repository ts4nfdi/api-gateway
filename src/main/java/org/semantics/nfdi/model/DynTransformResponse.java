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
            List<Map<String, Object>> nestedList = (List<Map<String, Object>>) nestedData;
            for (Map<String, Object> item : nestedList) {
                Map<String, Object> newItem = processItem(item, config);
                if (!newItem.isEmpty()) {
                    result.add(newItem);
                }
            }
        } else {
            logger.error("Expected List for nested JSON key: {}, but found: {}", nestedJsonKey, nestedData.getClass().getSimpleName());
        }

        logger.info("Transformed response: {}", result);
        return result;
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
                    logger.warn("Value for field {} is null", field);
                }
            } catch (Exception e) {
                logger.error("Error mapping response field: {}", field, e);
            }
        }

        return newItem;
    }
}
