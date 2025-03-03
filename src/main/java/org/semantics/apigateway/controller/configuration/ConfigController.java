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

    @Operation(summary = "Show all current enabled databases configurations (e.g BiodivPortal, AgroPortal, EBI, TIB etc.)")
    @ApiResponses(value = @ApiResponse(
            responseCode = "200", description = "Successful retrieval of terms",
            content = @Content(mediaType = "application/json", schema = @Schema(type = "array", implementation = DatabaseConfig.class)))
    )
    @GetMapping("/databases")
    public List<DatabaseConfig> getAllDatabases() {
        return configurationLoader.getDatabaseConfigs();
    }


    @Operation(summary = "Show all current enabled services configurations and endpoints mappings (e.g. OLS, OntoPortal)")
    @ApiResponses(value = @ApiResponse(
            responseCode = "200", description = "Successful retrieval of terms",
            content = @Content(mediaType = "application/json", schema = @Schema(type = "array", implementation = DatabaseConfig.class)))
    )
    @GetMapping("/services")
    public List<ServiceConfig> getAllServices() {
        return configurationLoader.getServiceConfigs();
    }


}
