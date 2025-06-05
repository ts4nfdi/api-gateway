package org.semantics.apigateway.service;

import lombok.Getter;
import org.semantics.apigateway.config.DatabaseConfig;
import org.semantics.apigateway.model.responses.AggregatedResourceBody;
import org.semantics.apigateway.model.responses.ApiResponse;
import org.semantics.apigateway.model.responses.TransformedApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
public class ResponseAggregatorService {

    private static final Logger logger = LoggerFactory.getLogger(ResponseAggregatorService.class);
    private final Class<? extends AggregatedResourceBody> clazz;

    public ResponseAggregatorService(Class<? extends AggregatedResourceBody> clazz) {
        this.clazz = clazz;
    }

    public TransformedApiResponse transformResponse(ApiResponse response, DatabaseConfig config, String endpoint, boolean paginate) {
        TransformedApiResponse newResponse = new TransformedApiResponse();
        newResponse.setOriginalResponse(response);

        if (response == null) {
            logger.error("Response is null");
            return newResponse;
        }

        if(config.getResponseMapping(endpoint) == null) {
            logger.error("Response mapping is not found for endpoint: {} for {}", endpoint, config.getName());
            return newResponse;
        }

        String nestedJsonKey = getNestedJsonKey(config, endpoint);
        Object nestedData = extractNestedData(response, nestedJsonKey);
        String paginationKey = config.getResponseMapping(endpoint).getPage();
        String totalCount = config.getResponseMapping(endpoint).getTotalCount();


        String paginationData = getData(response.getResponseBody(), paginationKey).orElse("0");
        String totalData = getData(response.getResponseBody(), totalCount).orElse("0");
        if(!paginationData.equals("0")) {
            newResponse.setPage(Integer.parseInt(paginationData));
            newResponse.setTotalCollections(Long.parseLong(totalData));
            newResponse.setPaginate(true);
        }

        if (nestedData == null) {
            logger.error("Expected List or Map for nested JSON key: {}, but found null", nestedJsonKey);
            return newResponse;
        }

        List<AggregatedResourceBody> result = transformData(nestedData, config, endpoint);
        newResponse.setCollection(result);

        logger.info("Transformed response: {} from  {}", result.size(), config.getName());
        return newResponse;
    }

    // Extract nested data from response body based on the key
    private Object extractNestedData(ApiResponse response, String nestedJsonKey) {
        Map<String, Object> responseBody = response.getResponseBody();

        if (nestedJsonKey != null && !nestedJsonKey.isEmpty()) {
            return responseBody.getOrDefault(nestedJsonKey, new ArrayList<>());
        }
        return responseBody;
    }

    // Transform nested data into a list of AggregatedResourceBody
    private List<AggregatedResourceBody> transformData(Object nestedData, DatabaseConfig config, String endpoint) {
        List<AggregatedResourceBody> result = new ArrayList<>();
        if (nestedData instanceof List && !((List) nestedData).isEmpty()) {
            processList((List<?>) nestedData, result, config, endpoint);
        } else if (nestedData instanceof Map && !((Map) nestedData).isEmpty()) {
            Object docs = extractDocsFromMap((Map<?, ?>) nestedData, config, endpoint);
            if(docs != null){
                processDocs(docs, result, config, endpoint);
            }
        }

        return result;
    }

    // Extract docs key from a Map based on configuration
    private Object extractDocsFromMap(Map<?, ?> nestedData, DatabaseConfig config, String endpoint) {
        String docsKey = getDocsKey(config, endpoint);
        return (docsKey != null && !docsKey.isEmpty()) ? nestedData.get(docsKey) : nestedData;
    }

    // Process the documents whether they are a List or single item
    private void processDocs(Object docs, List<AggregatedResourceBody> result, DatabaseConfig config, String endpoint) {
        if (docs instanceof List) {
            processList((List<?>) docs, result, config, endpoint);
        } else if (docs instanceof Map) {
            Map<?, ?> docsMap = (Map<?, ?>) docs;
            if(docsMap.containsKey("collection") && docsMap.size() == 1){
                processList((List<?>) docsMap.get("collection"), result, config, endpoint);
            } else {
                addNewItem(docs, result, config, endpoint);
            }
        } else {
            logger.error("Unexpected document type: {}. Expected List or Map.", docs.getClass().getSimpleName());
        }
    }

    private void addNewItem(Object docs, List<AggregatedResourceBody> result, DatabaseConfig config, String endpoint) {
        AggregatedResourceBody newItem = processItem((Map<String, Object>) docs, config, endpoint);
        if (newItem != null) {
            result.add(newItem);
        }
    }

    // Process list of items and add valid processed items to the result list
    private void processList(List<?> dataList, List<AggregatedResourceBody> result, DatabaseConfig config, String endpoint) {
        for (Object item : dataList) {
            if (item instanceof Map) {
                addNewItem(item, result, config, endpoint);
            } else {
                logger.warn("Skipping non-Map item: {}", item);
            }
        }
    }

    // Process individual map items into AggregatedResourceBody
    private AggregatedResourceBody processItem(Map<String, Object> item, DatabaseConfig config, String endpoint) {
        try {
            return AggregatedResourceBody.fromMap(item, config, endpoint, getClazz().getDeclaredConstructor().newInstance());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error processing item: {}", e.getMessage(), e);
            return null;
        }
    }

    // Helper to get the nested JSON key from config
    private String getNestedJsonKey(DatabaseConfig config, String endpoint) {
        return config.getResponseMapping(endpoint).getNestedJson();
    }

    // Helper to get the documents key from config
    private String getDocsKey(DatabaseConfig config, String endpoint) {
        return config.getResponseMapping(endpoint).getKey();
    }

    private Optional<String> getData(Map<String, Object> responseBody, String key) {
        Optional<String> result = Optional.empty();
        Object value = MappingTransformer.itemValueGetter(responseBody, key);
        if (value != null) {
            result = Optional.of(value.toString());
        }
        return result;
    }
}
