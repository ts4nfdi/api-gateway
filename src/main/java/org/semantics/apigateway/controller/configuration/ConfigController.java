package org.semantics.apigateway.controller.configuration;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.semantics.apigateway.config.DatabaseConfig;
import org.semantics.apigateway.config.ServiceConfig;
import org.semantics.apigateway.service.configuration.ConfigurationLoader;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/config")
@CrossOrigin
@Tag(name = "Configuration")
public class ConfigController {


    private final ConfigurationLoader configurationLoader;

    public ConfigController(ConfigurationLoader configurationLoader) {
        this.configurationLoader = configurationLoader;
    }

    @Operation(summary = "List the connected providers.",
            description = "Returns all providers (terminology services) the gateway currently queries, e.g. AgroPortal, BiodivPortal, EBI OLS4, or TIB, " +
                    "each with its `name`, its `type` (the kind of software it runs, e.g. `ols2`, `ontoportal`, `skosmos`), and the `url` of its API. " +
                    "Use the `name` or `type` values in the `database` parameter of the other endpoints.")
    @ApiResponses(value = @ApiResponse(
            responseCode = "200", description = "List of the connected providers",
            content = @Content(mediaType = "application/json", schema = @Schema(type = "array", implementation = DatabaseConfig.class)))
    )
    @GetMapping("/databases")
    public List<DatabaseConfig> getAllDatabases() {
        return configurationLoader.getDatabaseConfigs();
    }


    @Operation(summary = "List the supported provider types and their endpoint mappings.",
            description = "Returns, for each supported type of terminology service (e.g. OLS, OntoPortal, Skosmos), how the endpoints of the gateway are translated " +
                    "into requests to that service's API, and how the fields of its responses are mapped to the unified fields of the gateway. " +
                    "Mainly useful for developers and for operators adding new providers.")
    @ApiResponses(value = @ApiResponse(
            responseCode = "200", description = "List of the supported provider types and their mappings",
            content = @Content(mediaType = "application/json", schema = @Schema(type = "array", implementation = ServiceConfig.class)))
    )
    @GetMapping("/services")
    public List<ServiceConfig> getAllServices() {
        return configurationLoader.getServiceConfigs();
    }


}
