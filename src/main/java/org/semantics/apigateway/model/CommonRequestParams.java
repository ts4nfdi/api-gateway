package org.semantics.apigateway.model;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.ws.rs.QueryParam;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Data
@Component
public class CommonRequestParams {
    
    @QueryParam("database")
    @Parameter(
            name = "database",
            description = "Choose on which databases of backend type to run the search. Deprecated. Future implementors are encouraged to use 'source'.",
            deprecated = true
    )
    private String database = "";
    
    @QueryParam("targetDbSchema")
    @Parameter(name = "targetDbSchema", in = ParameterIn.QUERY, description = "Transform the response result to a specific schema. Deprecated. Future implementors are encouraged to use 'targetSchema'.", deprecated = true)
    private TargetSchema targetDbSchema;
    
    @QueryParam("source")
    @Parameter(
            name = "source",
            description = "Choose on which sources (formerly databases) of backend type to run the search"
    )
    private String source = "";
    
    @QueryParam("targetSchema")
    @Parameter(name = "targetSchema", in = ParameterIn.QUERY, description = "Transform the response result to a specific schema (formerly targetSchema)")
    private TargetSchema targetSchema;
    
    @QueryParam("showResponseConfiguration")
    private boolean showResponseConfiguration = false;

    @QueryParam("displayEmptyValues")
    private boolean displayEmptyValues = true;

    @QueryParam("disableCache")
    @Parameter(name = "disableCache", in = ParameterIn.QUERY, description = "Disable caching (not implemented yet)")
    private boolean disableCache = false;

    @QueryParam("display")
    @Parameter(name = "display", in = ParameterIn.QUERY, description = "Choose the attribute to display in the results (coma seperated)",
            array = @ArraySchema(schema = @Schema(type = "string")))
    private String display = "";


    public List<String> getDisplay() {
        List<String> result = new ArrayList<>();
//        result.add("iri"); // TODO remove this if the TSS no more use it and instead use the @id
        if (display != null && !display.isEmpty()) {
            result.addAll(Arrays.asList(display.split(",")));
        }
        return result;
    }
    
    public String getSource() {
        return StringUtils.isEmpty(source) ? database : source;
    }
    
    public TargetSchema getTargetSchema () {
        return targetSchema == null ? targetSchema : targetSchema;
    }
}
