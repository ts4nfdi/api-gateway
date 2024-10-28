package org.semantics.apigateway.service;

import lombok.Getter;
import org.apache.lucene.queryparser.classic.ParseException;
import org.semantics.apigateway.config.DatabaseConfig;
import org.semantics.apigateway.model.ResponseFormat;
import org.semantics.apigateway.model.TargetDbSchema;
import org.semantics.apigateway.model.responses.AggregatedApiResponse;
import org.semantics.apigateway.model.responses.ApiResponse;
import org.semantics.apigateway.model.responses.TransformedApiResponse;
import org.semantics.apigateway.service.search.SearchLocalIndexerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;


@Service
public class ArtefactsService  extends  AbstractEndpointService {

    public ArtefactsService(ConfigurationLoader configurationLoader) {
        super(configurationLoader);
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
}
