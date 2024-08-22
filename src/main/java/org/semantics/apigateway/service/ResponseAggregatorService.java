package org.semantics.apigateway.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.semantics.apigateway.config.OntologyConfig;
import org.semantics.apigateway.model.responses.ApiResponse;
import org.semantics.apigateway.model.responses.TransformedApiResponse;
import org.semantics.apigateway.model.responses.AggregatedResourceBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ResponseAggregatorService {

    private static final Logger logger = LoggerFactory.getLogger(ResponseAggregatorService.class);

    // Method to dynamically transform a response based on the provided OntologyConfig
    public TransformedApiResponse dynTransformResponse(ApiResponse response, OntologyConfig config) {
        TransformedApiResponse newResponse = new TransformedApiResponse();
        List<AggregatedResourceBody> result = new ArrayList<>();


        if (response == null) {
            logger.error("Response is null");
            return newResponse;
        }

        newResponse.setOriginalResponse(response);

        // Extracting the nested JSON key from the response based on the configuration
        String nestedJsonKey = config.getResponseMapping().getNestedJson();
        Object nestedData = response.getResponseBody().getOrDefault(nestedJsonKey, new ArrayList<>());
        logger.debug("Nested JSON key: {}", nestedJsonKey);
        logger.debug("Nested data type: {}", nestedData.getClass().getSimpleName());

        // Processing the data based on its type (List or Map)
        if (nestedData instanceof List) {
            processList((List<?>) nestedData, result, config);
        } else if (nestedData instanceof Map) {
            // Extracting the documents from the nested data
            String docsKey = config.getResponseMapping().getKey();
            Object docs = ((Map<?, ?>) nestedData).get(docsKey);
            if (docs instanceof List) {
                processList((List<?>) docs, result, config);
            } else {
                logger.error("Expected List for key '{}', but found: {}", docsKey, docs.getClass().getSimpleName());
            }
        } else {
            logger.error("Expected List or Map for nested JSON key: {}, but found: {}", nestedJsonKey, nestedData.getClass().getSimpleName());
        }

        logger.debug("Transformed response: {}", result);

        newResponse.setCollection(result);

        return newResponse;
    }

    // Helper method to process a list of data items
    private void processList(List<?> dataList, List<AggregatedResourceBody> result, OntologyConfig config) {
        for (Object item : dataList) {
            if (item instanceof Map) {
                // Processing each item in the list
                AggregatedResourceBody newItem = processItem((Map<String, Object>) item, config);
                if (newItem != null) {
                    result.add(newItem);
                }
            }
        }
    }

    // Method to process individual items in the response
    private AggregatedResourceBody processItem(Map<String, Object> item, OntologyConfig config) {
        try {
            return AggregatedResourceBody.fromMap(item, config);
        } catch (Exception e) {
            logger.error("Error processing item: {}", e.getMessage(), e);
            return null;
        }
    }


}
