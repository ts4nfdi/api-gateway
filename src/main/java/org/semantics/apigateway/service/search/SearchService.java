package org.semantics.apigateway.service.search;

import org.apache.lucene.queryparser.classic.ParseException;
import org.semantics.apigateway.model.ResponseFormat;
import org.semantics.apigateway.model.TargetDbSchema;
import org.semantics.apigateway.model.responses.AggregatedApiResponse;
import org.semantics.apigateway.model.user.TerminologyCollection;
import org.semantics.apigateway.model.user.User;
import org.semantics.apigateway.service.AbstractEndpointService;
import org.semantics.apigateway.service.ApiAccessor;
import org.semantics.apigateway.service.JsonLdTransform;
import org.semantics.apigateway.service.ResponseTransformerService;
import org.semantics.apigateway.service.auth.CollectionService;
import org.semantics.apigateway.service.configuration.ConfigurationLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


@Service
public class SearchService extends AbstractEndpointService {


    private final SearchLocalIndexerService localIndexer;

    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);
    private final CollectionService collectionService;

    public SearchService(ConfigurationLoader configurationLoader, SearchLocalIndexerService localIndexer, CacheManager cacheManager, JsonLdTransform jsonLdTransform, ResponseTransformerService responseTransformerService, CollectionService collectionService) {
        super(configurationLoader, cacheManager, jsonLdTransform, responseTransformerService);
        this.localIndexer = localIndexer;
        this.collectionService = collectionService;
    }

    public CompletableFuture<Object> performSearch(String query, String database, String format, String targetDbSchema, boolean showResponseConfiguration) {
        ResponseFormat responseFormat = format == null ? ResponseFormat.json : ResponseFormat.valueOf(format);
        TargetDbSchema targetDbSchemaEnum = targetDbSchema == null ? null : TargetDbSchema.valueOf(targetDbSchema);
        return performSearch(query, database, responseFormat, targetDbSchemaEnum, showResponseConfiguration, null, null, null, null);
    }

    // Performs a federated search across multiple databases and optionally transforms the results for a target database schema]
    public CompletableFuture<Object> performSearch(
            String query, String database, ResponseFormat format,
            TargetDbSchema targetDbSchema, boolean showResponseConfiguration,
            String[] terminologies,
            String collectionId,
            User currentUser) {
        return performSearch(query, database, format, targetDbSchema, showResponseConfiguration, terminologies, collectionId, currentUser, null);
    }

    // Performs a federated search across multiple databases and optionally transforms the results for a target database schema]
    public CompletableFuture<Object> performSearch(
            String query, String database, ResponseFormat format,
            TargetDbSchema targetDbSchema, boolean showResponseConfiguration,
            String[] terminologies,
            String collectionId,
            User currentUser,
            ApiAccessor accessor) {

        CompletableFuture<Object> future = new CompletableFuture<>();
        Map<String, String> apiUrls;

        try {
            apiUrls = filterDatabases(database, "search");
        } catch (Exception e) {
            future.completeExceptionally(e);
            return future;
        }

        if (accessor == null) {
            accessor = getAccessor();
        }
        accessor.setUrls(apiUrls);
        accessor.setLogger(logger);

        TerminologyCollection collection = collectionService.getCurrentUserCollection(collectionId, currentUser);

        return accessor.get(query)
                .thenApply(data -> this.transformApiResponses(data, "search"))
                .thenApply(transformedData -> flattenResponseList(transformedData, showResponseConfiguration, collection))
                .thenApply(data -> filterOutByTerminologies(terminologies, data))
                .thenApply(data -> filterOutByCollection(collection , data))
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
