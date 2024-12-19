package org.semantics.apigateway.service.search;

import lombok.Getter;
import org.apache.lucene.queryparser.classic.ParseException;
import org.semantics.apigateway.config.DatabaseConfig;
import org.semantics.apigateway.model.BackendType;
import org.semantics.apigateway.model.ResponseFormat;
import org.semantics.apigateway.model.TargetDbSchema;
import org.semantics.apigateway.model.responses.AggregatedApiResponse;
import org.semantics.apigateway.model.responses.ApiResponse;
import org.semantics.apigateway.model.responses.TransformedApiResponse;
import org.semantics.apigateway.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


@Service
public class SearchService extends AbstractEndpointService {


    private final SearchLocalIndexerService localIndexer;

    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);

    public SearchService(ConfigurationLoader configurationLoader, SearchLocalIndexerService localIndexer, ApiAccessor apiAccessor, JsonLdTransform jsonLdTransform, ResponseTransformerService responseTransformerService) {
        super(configurationLoader, apiAccessor, jsonLdTransform, responseTransformerService);
        this.localIndexer = localIndexer;
    }


    public CompletableFuture<Object> performSearch(
            String query, String database, ResponseFormat format,
            TargetDbSchema targetDbSchema, boolean showResponseConfiguration) {

        String db = "", formatStr = "", target = "";

        if (database != null) {
            db = database.toString();
        }
        if (format != null) {
            formatStr = format.toString();
        }
        if (targetDbSchema != null) {
            target = targetDbSchema.toString();
        }


        return performSearch(query, db, formatStr, target, showResponseConfiguration);
    }

    // Performs a federated search across multiple databases and optionally transforms the results for a target database schema]
    public CompletableFuture<Object> performSearch(
            String query, String database, String format,
            String targetDbSchema, boolean showResponseConfiguration) {

        CompletableFuture<Object> future = new CompletableFuture<>();
        Map<String, String> apiUrls;

        try {
             apiUrls = filterDatabases(database, "search");
        }catch (Exception e) {
            future.completeExceptionally(e);
            return future;
        }

        getAccessor().setUrls(apiUrls);
        getAccessor().setLogger(logger);

        return getAccessor().get(query)
                .thenApply(data -> this.transformApiResponses(data, "search"))
                .thenApply(transformedData -> flattenResponseList(transformedData, showResponseConfiguration))
                .thenApply(data -> reIndexResults(query, data))
                .thenApply(data -> transformJsonLd(data, format))
                .thenApply(data -> transformForTargetDbSchema(data, targetDbSchema));
    }


    private AggregatedApiResponse reIndexResults(String query, AggregatedApiResponse data) {
        List<Map<String, Object>> collection = data.getCollection();
        try {
            collection = this.localIndexer.reIndexResults(query.replace("*", ""), collection, logger);
        } catch (IOException | ParseException e) {
            throw new RuntimeException("Error during re-indexing results", e);
        }
        data.setCollection(collection);
        return data;
    }

}
