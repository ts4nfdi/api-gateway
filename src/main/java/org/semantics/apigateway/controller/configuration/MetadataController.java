package org.semantics.apigateway.controller.configuration;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.semantics.apigateway.service.configuration.MetadataService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public Object getArtefactMetadata() {
        return metadataService.getArtefactMetadata();
    }

    @Operation(summary = "Get information about the artefact terms metamodel")
    @GetMapping("/term")
    public Object getTermMetadata() {
        return metadataService.getTermMetadata();
    }

    @Operation(summary = "Get information about the artefact search metamodel")
    @GetMapping("/search")
    public Object getSearchMetadata() {
        return metadataService.getSearchMetadata();
    }
}
