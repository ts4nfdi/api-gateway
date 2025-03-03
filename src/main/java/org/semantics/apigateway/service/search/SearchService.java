package org.semantics.apigateway.service.search;

import org.apache.lucene.queryparser.classic.ParseException;
import org.semantics.apigateway.model.ResponseFormat;
import org.semantics.apigateway.model.TargetDbSchema;
import org.semantics.apigateway.model.responses.AggregatedApiResponse;
import org.semantics.apigateway.model.user.TerminologyCollection;
import org.semantics.apigateway.model.user.User;
import org.semantics.apigateway.service.*;
import org.semantics.apigateway.service.auth.CollectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


@Service
public class SearchService extends AbstractEndpointService {


    private final SearchLocalIndexerService localIndexer;

    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);
    private final CollectionService collectionService;

    public SearchService(ConfigurationLoader configurationLoader, SearchLocalIndexerService localIndexer, ApiAccessor apiAccessor, JsonLdTransform jsonLdTransform, ResponseTransformerService responseTransformerService, CollectionService collectionService) {
        super(configurationLoader, apiAccessor, jsonLdTransform, responseTransformerService);
        this.localIndexer = localIndexer;
        this.collectionService = collectionService;
    }

    public CompletableFuture<Object> performSearch(String query, String database, String format, String targetDbSchema, boolean showResponseConfiguration) {
        ResponseFormat responseFormat = format == null ? null : ResponseFormat.valueOf(format);
        TargetDbSchema targetDbSchemaEnum = targetDbSchema == null ? null : TargetDbSchema.valueOf(targetDbSchema);
        return performSearch(query, database, responseFormat, targetDbSchemaEnum, showResponseConfiguration, null, null, null);
    }

    public CompletableFuture<Object> performSearch(
            String query, String database,
            ResponseFormat format,
            TargetDbSchema targetDbSchema, boolean showResponseConfiguration,
            String[] terminologies,
            String collectionId,
            User user) {

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


        return performSearch(query, db, formatStr, target, showResponseConfiguration, terminologies, collectionId, user);
    }

    // Performs a federated search across multiple databases and optionally transforms the results for a target database schema]
    public CompletableFuture<Object> performSearch(
            String query, String database, String format,
            String targetDbSchema, boolean showResponseConfiguration,
            String[] terminologies,
            String collectionId,
            User currentUser) {

        CompletableFuture<Object> future = new CompletableFuture<>();
        Map<String, String> apiUrls;

        try {
            apiUrls = filterDatabases(database, "search");
        } catch (Exception e) {
            future.completeExceptionally(e);
            return future;
        }

        getAccessor().setUrls(apiUrls);
        getAccessor().setLogger(logger);

        TerminologyCollection collection = collectionService.getCurrentUserCollection(collectionId, currentUser);

        return getAccessor().get(query)
                .thenApply(data -> this.transformApiResponses(data, "search"))
                .thenApply(transformedData -> flattenResponseList(transformedData, showResponseConfiguration, collection))
                .thenApply(data -> filterOutByTerminologies(terminologies, collection , data))
                .thenApply(data -> reIndexResults(query, data))
                .thenApply(data -> transformJsonLd(data, format))
                .thenApply(data -> transformForTargetDbSchema(data, targetDbSchema));
    }


    private AggregatedApiResponse filterOutByTerminologies(String[] terminologies, TerminologyCollection terminologiesCollection, AggregatedApiResponse data) {
        String[] finalTerminologies;

        if (terminologiesCollection != null) {
            finalTerminologies = terminologiesCollection.getTerminologies().toArray(new String[0]);
        } else {
            finalTerminologies = terminologies;
        }

        if (finalTerminologies == null || finalTerminologies.length == 0) {
            return data;
        }

        List<Map<String, Object>> collection = data.getCollection();
        collection = collection.stream()
                .filter(map -> {
                    String terminology = (String) map.get("ontology");
                    return Arrays.stream(finalTerminologies).map(String::toLowerCase).toList().contains(terminology.toLowerCase());
                })
                .collect(Collectors.toList());
        data.setCollection(collection);

        return data;
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
