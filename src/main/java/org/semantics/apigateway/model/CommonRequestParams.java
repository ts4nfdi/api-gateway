package org.semantics.apigateway.model;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.ws.rs.QueryParam;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Data
@Component
public class CommonRequestParams {

    @QueryParam("database")
    @Parameter(name = "database", in = ParameterIn.QUERY, required = false,
            description =
                    "Comma-separated list of providers to query, matched against the configured provider names or types (e.g. ols2, ontoportal, skosmos, tib, zbmed). " +
                    "Unknown values result in an error listing the possible values. " +
                    "If omitted, all configured providers are queried.",
            schema = @Schema(type = "string", defaultValue = "", example = "ols2"))
    private String database = "";

    @QueryParam("targetDbSchema")
    @Parameter(name = "targetDbSchema", in = ParameterIn.QUERY, required = false,
            description =
                    "Transforms the response into the schema of the given provider type. " +
                    "If omitted, the gateway's own aggregated response format is returned.",
            schema = @Schema(implementation = TargetDbSchema.class, example = "ols2"))
    private TargetDbSchema targetDbSchema;

    @QueryParam("collectionId")
    @Parameter(name = "collectionId", in = ParameterIn.QUERY, required = false,
            description =
                    "UUID or W3ID of a terminology collection. Restricts the response to artefacts contained in that terminology collection. " +
                    "The terminology collection must be accessible to the current user, otherwise the request is rejected. " +
                    "If omitted, no collection filter is applied.",
            schema = @Schema(type = "string", format = "uuid"))
    private String collectionId;

    @QueryParam("showResponseConfiguration")
    @Parameter(name = "showResponseConfiguration", in = ParameterIn.QUERY, required = false,
            description =
                    "If true, the response additionally contains the original, untransformed responses of the providers and the request configuration used to query them. " +
                    "If omitted, defaults to false.",
            schema = @Schema(type = "boolean", defaultValue = "false"))
    private boolean showResponseConfiguration = false;

    @QueryParam("displayEmptyValues")
    @Parameter(name = "displayEmptyValues", in = ParameterIn.QUERY, required = false,
            description =
                    "If false, attributes without a value are removed from the result items. " +
                    "If omitted, defaults to true.",
            schema = @Schema(type = "boolean", defaultValue = "true"))
    private boolean displayEmptyValues = true;

    @QueryParam("disableCache")
    @Parameter(name = "disableCache", in = ParameterIn.QUERY, required = false,
            description =
                    "Disable caching of provider responses (not implemented yet, currently has no effect). " +
                    "If omitted, defaults to false.",
            schema = @Schema(type = "boolean", defaultValue = "false"))
    private boolean disableCache = false;

    @QueryParam("display")
    @Parameter(name = "display", in = ParameterIn.QUERY, required = false,
            description =
                    "Comma-separated list of attributes to include in the result items; all other attributes are removed. " +
                    "If omitted, all attributes are returned.",
            array = @ArraySchema(schema = @Schema(type = "string", example = "iri,label")))
    private String display = "";

    public List<String> getDisplay() {
        List<String> result = new ArrayList<>();
//        result.add("iri"); // TODO remove this if the TSS no more use it and instead use the @id
        if (display != null && !display.isEmpty()) {
            result.addAll(Arrays.asList(display.split(",")));
        }
        return result;
    }
    
    @QueryParam("timeout")
    @Parameter(name = "timeout", in = ParameterIn.QUERY, required = false,
            description =
                    "Maximum time span in milliseconds the gateway waits for each queried provider to respond. " +
                    "The providers are queried in parallel. " +
                    "Providers that do not answer within the timeout, or that answer with an error, are skipped; " +
                    "the results of all other providers are returned as usual. " +
                    "Use the `database` parameter to exclude slow providers. " +
                    "The value is limited by a configured maximum (default 60000 ms = 60 seconds). " +
                    "If omitted, negative, or larger than the maximum, the maximum is used. " +
                    "See *The timeout parameter* section of the API description for details.",
            schema = @Schema(type = "integer", format = "int64", defaultValue = "60000", example = "60000"))
    private long timeout = 60*1000;
    
    @QueryParam("omitArtefactsWithoutIri")
    @Parameter(name = "omitArtefactsWithoutIri", in = ParameterIn.QUERY, required = false,
            description =
                    "If true, only artefacts that have a proper IRI as their identifier are returned; all others are filtered out. " +
                    "If omitted, defaults to false.",
            schema = @Schema(type = "boolean", defaultValue = "false"))
    private boolean omitArtefactsWithoutIri = false;
}
