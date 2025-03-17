package org.semantics.apigateway.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.semantics.apigateway.model.SemanticArtefactCatalog;
import org.semantics.apigateway.service.StatusService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/")
@CrossOrigin
public class GatewayController {


    private final StatusService statusService;

    public GatewayController(StatusService statusService) {
        this.statusService = statusService;
    }


    @GetMapping(path = "/")
    public SemanticArtefactCatalog home(HttpServletRequest request) throws IOException {
        SemanticArtefactCatalog catalog = new SemanticArtefactCatalog();
        catalog.setId(statusService.getBaseUrl(request));
        catalog.setType("https://w3id.org/mod#SemanticArtefactCatalog");
        catalog.setTitle("API Gateway");
        catalog.setDescription("The TS4NFDI Federated Service is an advanced, dynamic solution designed to perform federated calls across multiple Terminology Services (TS) within NFDI. It is particularly tailored for environments where integration and aggregation of diverse data sources are essential. The service offers search capabilities, enabling users to refine search results based on specific criteria, and supports responses in both JSON and JSON-LD formats.");
        catalog.setStatus("alpha");
        catalog.setLicense("https://opensource.org/licenses/BSD-2-Clause");
        catalog.setLinks(
                Map.of(
                        "documentation", statusService.getBaseUrl(request) + "/doc/api",
                        "search", statusService.getBaseUrl(request) + "/search",
                        "artefacts", statusService.getBaseUrl(request) + "/artefacts",
                        "examples", statusService.getBaseUrl(request) + "/status/examples"
                )
        );
        Map<String, String> context = getCatalogContextMap();
        catalog.setContext(context);

        return catalog;
    }

    private static Map<String, String> getCatalogContextMap() {
        //TODO: do this dynamically in the future
        Map<String,String> context = new HashMap<>();
        context.put("title", "http://purl.org/dc/terms/title");
        context.put("description", "http://purl.org/dc/terms/description");
        context.put("identifier", "http://purl.org/dc/terms/identifier");
        context.put("status", "https://w3id.org/mod#status");
        context.put("accessRights", "http://purl.org/dc/terms/accessRights");
        context.put("created", "http://purl.org/dc/terms/created");
        context.put("license", "http://purl.org/dc/terms/license");
        context.put("language", "http://purl.org/dc/terms/language");
        context.put("keyword", "http://www.w3.org/ns/dcat#keyword");
        context.put("bibliographicCitation", "http://purl.org/dc/terms/bibliographicCitation");
        context.put("subject", "http://purl.org/dc/terms/subject");
        context.put("coverage", "http://purl.org/dc/terms/coverage");
        context.put("createdWith", "http://purl.org/pav/createdWith");
        context.put("accrualMethod", "http://purl.org/dc/terms/accrualMethod");
        context.put("accrualPeriodicity", "http://purl.org/dc/terms/accrualPeriodicity");
        context.put("wasGeneratedBy", "http://www.w3.org/ns/prov#wasGeneratedBy");
        return context;
    }


    @GetMapping(path = "doc/api", produces = {MediaType.TEXT_HTML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Get API documentation")
    public Object doc(
            @RequestHeader(value = "Accept", defaultValue = MediaType.TEXT_HTML_VALUE) String accept,
            HttpServletRequest request, HttpServletResponse response
    ) throws IOException {
        String redirectUrl;
        if (accept.contains(MediaType.APPLICATION_JSON_VALUE) || accept.contains("application/json+ld")) {
            redirectUrl = "/v3/api-docs";
            return statusService.getResultFromUrlReactive(redirectUrl);
        } else {
            redirectUrl = request.getContextPath() + "/swagger-ui/index.html?configUrl=/api-gateway/openapi/swagger-config";
            response.setStatus(HttpStatus.MOVED_PERMANENTLY.value());
            response.setHeader("Location", redirectUrl);
            return redirectUrl;
        }

    }


}
