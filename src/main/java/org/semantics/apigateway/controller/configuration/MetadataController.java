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

    @Operation(summary = "Show how artefact metadata fields are mapped per provider type.",
            description = "Returns, for each provider type (e.g. `ols2`, `ontoportal`, `skosmos`), which field of the provider's response is used for which " +
                    "artefact field of the gateway (e.g. `label`, `descriptions`, `version`). " +
                    "Helps to understand where a value in a `GET /artefacts` response comes from, or why it is empty for some providers.")
    @GetMapping("/artefact")
    public Object getArtefactMetadata() {
        return metadataService.getArtefactMetadata();
    }

    @Operation(summary = "Show how term fields are mapped per provider type.",
            description = "Returns, for each provider type (e.g. `ols2`, `ontoportal`, `skosmos`), which field of the provider's response is used for which " +
                    "term field of the gateway (e.g. `label`, `synonyms`, `descriptions`). " +
                    "Helps to understand where a value in a class, concept, property, or individual comes from, or why it is empty for some providers.")
    @GetMapping("/term")
    public Object getTermMetadata() {
        return metadataService.getTermMetadata();
    }

    @Operation(summary = "Show how search result fields are mapped per provider type.",
            description = "Returns, for each provider type (e.g. `ols2`, `ontoportal`, `skosmos`), which field of the provider's response is used for which " +
                    "field of a search result. Currently returns the same mapping as `GET /config/metadata/term`.")
    @GetMapping("/search")
    public Object getSearchMetadata() {
        return metadataService.getSearchMetadata();
    }
}
