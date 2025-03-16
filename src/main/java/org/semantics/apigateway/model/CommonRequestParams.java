package org.semantics.apigateway.model;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.ws.rs.QueryParam;
import lombok.Data;
import org.springframework.stereotype.Component;


@Data
@Component
public class CommonRequestParams {

    @QueryParam("database")
    @Parameter(
            name = "database",
            description = "Choose on which databases of backend type to run the search"
    )
    private String database = "";

    @QueryParam("format")
    @Parameter(name = "format", in = ParameterIn.QUERY, description = "Response format")
    private ResponseFormat format;

    @QueryParam("targetDbSchema")
    @Parameter(name = "targetDbSchema", in = ParameterIn.QUERY, description = "Transform the response result to a specific schema")
    private TargetDbSchema targetDbSchema;

    @QueryParam("showResponseConfiguration")
    private boolean showResponseConfiguration = false;

    @QueryParam("disableCache")
    @Parameter(name = "disableCache", in = ParameterIn.QUERY, description = "Disable caching (not implemented yet)")
    private boolean disableCache = false;

    @QueryParam("display")
    @Parameter(name = "display", in = ParameterIn.QUERY, description = "Choose the attribute to display in the results (coma seperated)",
            array = @ArraySchema(schema = @Schema(type = "string")))
    private String display;
}
