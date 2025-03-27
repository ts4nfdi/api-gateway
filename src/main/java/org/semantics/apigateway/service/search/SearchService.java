package org.semantics.apigateway.service.search;

import org.apache.lucene.queryparser.classic.ParseException;
import org.semantics.apigateway.model.CommonRequestParams;
import org.semantics.apigateway.model.RDFResource;
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


@Service
public class SearchService extends AbstractEndpointService {


    private final SearchLocalIndexerService localIndexer;

    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);
    private final CollectionService collectionService;

    public SearchService(ConfigurationLoader configurationLoader, SearchLocalIndexerService localIndexer, CacheManager cacheManager, JsonLdTransform jsonLdTransform, ResponseTransformerService responseTransformerService, CollectionService collectionService) {
        super(configurationLoader, cacheManager, jsonLdTransform, responseTransformerService, RDFResource.class);
        this.localIndexer = localIndexer;
        this.collectionService = collectionService;
    }

    public Object performSearch(String query, String database,  String targetDbSchema, boolean showResponseConfiguration) {
        TargetDbSchema targetDbSchemaEnum = targetDbSchema == null ? null : TargetDbSchema.valueOf(targetDbSchema);
        CommonRequestParams commonRequestParams = new CommonRequestParams();
        commonRequestParams.setDatabase(database);
        commonRequestParams.setTargetDbSchema(targetDbSchemaEnum);
        commonRequestParams.setShowResponseConfiguration(showResponseConfiguration);
        return performSearch(query, commonRequestParams, null, null, null, null);
    }

    public Object performSearch(
            String query,
            CommonRequestParams params,
            String[] terminologies,
            String collectionId,
            User currentUser,
            ApiAccessor accessor) {
        String endpoint = "search";
        String database = params.getDatabase();
        TargetDbSchema targetDbSchema = params.getTargetDbSchema();
        TerminologyCollection collection = collectionService.getCurrentUserCollection(collectionId, currentUser);
        accessor = initAccessor(database, endpoint, accessor);

        try {
            return accessor.get(query)
                    .thenApply(data -> this.transformApiResponses(data, endpoint))
                    .thenApply(transformedData -> flattenResponseList(transformedData, params, collection))
                    .thenApply(data -> filterOutByTerminologies(terminologies, data))
                    .thenApply(data -> filterOutByCollection(collection, data))
                    .thenApply(data -> reIndexResults(query, data))
                    .thenApply(x -> transformJsonLd(x, params))
                    .thenApply(data -> transformForTargetDbSchema(data, targetDbSchema, endpoint)).get();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
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
