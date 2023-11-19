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
            return result;
        }

        String responseKey = config.getApiKey();
        Object responseData = response.getOrDefault(responseKey, new HashMap<>());
        logger.info("key: {}", responseKey);
        logger.info("Response: {}", response);
        logger.info("Response data type: {}", responseData.getClass().getSimpleName());

        if (responseData instanceof List) {
            List<Object> keys = (List<Object>) responseData;
            for (Object key : keys) {
                processKey(key, config, result);
            }
        } else if (responseData instanceof Map) {
            processKey(responseData, config, result);
        } else {
            logger.error("Unexpected data type for key: {}. Type is: {}", responseKey, responseData.getClass().getSimpleName());
        }

        logger.info("Transformed response: {}", result);
        return result;
    }

    private void processKey(Object key, OntologyConfig config, List<Map<String, Object>> result) {
        if (key == null) {
            return;
        }

        Map<String, Object> keyMap = (Map<String, Object>) key;
        Map<String, Object> newItem = new HashMap<>();

        ResponseMapping responseMapping = config.getResponseMapping(); 

        List<String> fieldList = ((ResponseMapping) responseMapping).getFieldList(); 

        for (String field : fieldList) {
            try {
                Object value = PropertyUtils.getNestedProperty(keyMap, field);
                newItem.put(field, value);
                logger.info("Accessed field: {} with value: {}", field, value);
            } catch (Exception e) {
                logger.error("Error mapping response field: {}", field, e);
            }
        }

        result.add(newItem);
    }
}
