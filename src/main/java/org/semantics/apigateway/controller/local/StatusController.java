package org.semantics.apigateway.controller.local;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.semantics.apigateway.model.responses.ResponseConfig;
import org.semantics.apigateway.service.StatusService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.Map;


@RestController
@RequestMapping("/status")
@CrossOrigin
@Tag(name = "Status")
public class StatusController {

    public enum EndpointsChecks {
        data, metadata, search, all
    }

    private final StatusService examplesService;

    public StatusController(StatusService examplesService) {
        this.examplesService = examplesService;
    }

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }


    @Operation(summary = "Get all examples we provide to have a better understanding of the API")
    @GetMapping("/examples")
    public ResponseEntity<Map<String, Map<String, String>>> getAllExamples() {
        return ResponseEntity.ok(examplesService.getAllExamples());
    }


    @Operation(summary = "Check the endpoint status and get the examples")
    @GetMapping("/check")
    @ApiResponse(responseCode = "200", description = "Endpoint status and examples",
            content = @Content(schema = @Schema(implementation = ResponseConfig.class)))
    public ResponseEntity<Map<String,Object>> runCheck(
            @Parameter(description = "Endpoint to check (e.g /artefacts)", required = true)
            @RequestParam String endpoint) throws UnsupportedEncodingException {
        return ResponseEntity.ok(examplesService.checkEndpoint(endpoint));
    }
}
