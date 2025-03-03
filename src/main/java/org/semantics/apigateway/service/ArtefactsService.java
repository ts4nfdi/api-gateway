package org.semantics.apigateway.service;

import org.semantics.apigateway.model.ResponseFormat;
import org.semantics.apigateway.model.TargetDbSchema;
import org.semantics.apigateway.model.responses.AggregatedApiResponse;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


@Service
public class ArtefactsService extends AbstractEndpointService {

    public ArtefactsService(ConfigurationLoader configurationLoader, ApiAccessor apiAccessor, JsonLdTransform transform, ResponseTransformerService responseTransformerService) {
        super(configurationLoader, apiAccessor, transform, responseTransformerService);
    }

    public CompletableFuture<Object> getArtefacts(String database, ResponseFormat format, TargetDbSchema targetDbSchema, boolean showResponseConfiguration) {

        String db = "", formatStr, target;

        if (database != null) {
            db = database.toString();
        }
        if (format != null) {
            formatStr = format.toString();
        } else {
            formatStr = "";
        }
        if (targetDbSchema != null) {
            target = targetDbSchema.toString();
        } else {
            target = "";
        }

        CompletableFuture<Object> future = new CompletableFuture<>();
        Map<String, String> apiUrls;

        try {
            apiUrls = filterDatabases(db, "resources");
        } catch (Exception e) {
            future.completeExceptionally(e);
            return future;
        }

        getAccessor().setUrls(apiUrls);
        getAccessor().setLogger(logger);

        return getAccessor().get()
                .thenApply(data -> this.transformApiResponses(data, "resources"))
                .thenApply(transformedData -> flattenResponseList(transformedData, showResponseConfiguration))
                .thenApply(data -> transformJsonLd(data, formatStr))
                .thenApply(data -> transformForTargetDbSchema(data, target));
    }

    public CompletableFuture<Object> getArtefact(String id, ResponseFormat format, TargetDbSchema targetDbSchema, boolean showResponseConfiguration) {

        Map<String, String> apiUrls = filterDatabases("", "resource_details");
        String formatStr, target;

        if (format != null) {
            formatStr = format.toString();
        } else {
            formatStr = "";
        }

        if (targetDbSchema != null) {
            target = targetDbSchema.toString();
        } else {
            target = "";
        }

        getAccessor().setUrls(apiUrls);
        getAccessor().setLogger(logger);
        getAccessor().setUnDecodeUrl(true);

        return getAccessor().get(id.toUpperCase())
                .thenApply(data -> this.transformApiResponses(data, "resource_details"))
                .thenApply(transformedData -> flattenResponseList(transformedData, showResponseConfiguration))
                .thenApply(data -> filterArtefactsById(data, id))
                .thenApply(data -> transformJsonLd(data, formatStr))
                .thenApply(data -> transformForTargetDbSchema(data, target));
    }


    public CompletableFuture<Object> getArtefactTerm(String id, String uri, ResponseFormat format, TargetDbSchema targetDbSchema, boolean showResponseConfiguration) {
        Map<String, String> apiUrls = filterDatabases("", "concept_details");
        String formatStr, target;

        if (format != null) {
            formatStr = format.toString();
        } else {
            formatStr = "";
        }

        if (targetDbSchema != null) {
            target = targetDbSchema.toString();
        } else {
            target = "";
        }

        getAccessor().setUrls(apiUrls);
        getAccessor().setLogger(logger);

        String encodedUrl = URLEncoder.encode(uri, StandardCharsets.UTF_8);

        return getAccessor().get(id.toUpperCase(), encodedUrl)
                .thenApply(data -> this.transformApiResponses(data, "concept_details"))
                .thenApply(transformedData -> flattenResponseList(transformedData, showResponseConfiguration))
                .thenApply(data -> transformJsonLd(data, formatStr))
                .thenApply(data -> transformForTargetDbSchema(data, target));
    }


    private AggregatedApiResponse filterArtefactsById(AggregatedApiResponse transformedResponse, String id) {
        List<Map<String, Object>> filtredList = transformedResponse.getCollection().stream().filter(x -> x.getOrDefault("label", "").toString().equalsIgnoreCase(id)).collect(Collectors.toList());
        transformedResponse.setCollection(filtredList);
        return transformedResponse;
    }
}
