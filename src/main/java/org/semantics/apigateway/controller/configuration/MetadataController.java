package org.semantics.apigateway.controller.configuration;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.semantics.apigateway.config.ResponseMapping;
import org.semantics.apigateway.service.configuration.MetadataService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/config/metadata")
@CrossOrigin
@Tag(name = "Configuration")
public class MetadataController {

    private final MetadataService metadataService;

    public MetadataController(MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    @Operation(summary = "Get information about the artefacts metamodel")
    @GetMapping("/artefact")
    public Map<String, ResponseMapping> getArtefactMetadata() {
        return metadataService.getArtefactMetadata();
    }

    @Operation(summary = "Get information about the artefact terms metamodel")
    @GetMapping("/term")
    public Map<String, ResponseMapping> getTermMetadata() {
        return metadataService.getTermMetadata();
    }

    @Operation(summary = "Get information about the artefact search metamodel")
    @GetMapping("/search")
    public Map<String, ResponseMapping> getSearchMetadata() {
        return metadataService.getSearchMetadata();
    }
}
