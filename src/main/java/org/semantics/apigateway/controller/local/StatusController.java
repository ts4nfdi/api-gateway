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
    @Operation(summary = "Check whether the gateway is running.",
            description = "Returns `pong` if the gateway is up. Does not contact any provider.")
    public String ping() {
        return "pong";
    }


    @Operation(summary = "Get example requests.",
            description = "Returns a list of ready-to-use example requests, grouped into `data` (terms of an artefact), `metadata` (artefacts), and `search`. " +
                    "Each example has a short title and a gateway path that you can call directly or pass to `GET /status/check`.")
    @GetMapping("/examples")
    public ResponseEntity<Map<String, Map<String, String>>> getAllExamples() {
        return ResponseEntity.ok(examplesService.getAllExamples());
    }


    @Operation(summary = "Check how the providers respond to a request.",
            description = "Runs the given gateway request and, instead of its results, returns statistics about how the providers responded: " +
                    "the URL called at each provider with its HTTP status code and response time, the total response time, the total number of results, " +
                    "and how completely the result fields were filled. " +
                    "Useful to find slow or failing providers.\n\n" +
                    "Example: `GET /status/check?endpoint=/search?query=plant`")
    @GetMapping("/check")
    @ApiResponse(responseCode = "200", description = "Statistics about the responses of the providers",
            content = @Content(schema = @Schema(implementation = ResponseConfig.class)))
    public ResponseEntity<Map<String,Object>> runCheck(
            @Parameter(description = "Gateway path to check, optionally with query parameters, e.g. `/artefacts` or `/search?query=plant`", required = true)
            @RequestParam String endpoint) {
        return ResponseEntity.ok(examplesService.checkEndpoint(endpoint));
    }
}
