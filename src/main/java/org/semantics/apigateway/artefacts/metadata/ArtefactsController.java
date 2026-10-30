package org.semantics.apigateway.artefacts.metadata;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.semantics.apigateway.model.CommonRequestParams;
import org.semantics.apigateway.model.user.User;
import org.semantics.apigateway.service.auth.AuthService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/")
@Tag(name = "MOD - Artefacts")
@CrossOrigin
public class ArtefactsController {
    private final ArtefactsService artefactsService;
    private final AuthService authService;

    public ArtefactsController(ArtefactsService artefactsService, AuthService authService) {
        this.artefactsService = artefactsService;
        this.authService = authService;
    }


    @GetMapping("/artefacts")
    @Operation(summary = "List all artefacts of all connected providers.",
            description = "Returns the metadata of all artefacts (ontologies, thesauri, vocabularies) that the connected providers offer, " +
                    "e.g. `label`, `descriptions`, `version`, `iri`, the short name `short_form`, and the hosting provider (`source_name`). " +
                    "Use the `short_form` or id of an artefact as `{id}` in the other `/artefacts/{id}/...` endpoints. " +
                    "Use `database` to list only the artefacts of some providers and `collectionId` to list only the artefacts of a terminology collection. " +
                    "If you are logged in, your private terminology collections can be used as well. " +
                    "As the list can be long, `display` (e.g. `display=short_form,label,source_name`) helps to keep the response small.\n\n" +
                    "Example: `GET /artefacts?database=tib&display=short_form,label`")
    @SecurityRequirement(name = "BearerAuth")
    public Object getArtefacts(@ParameterObject CommonRequestParams params) {
        User user = authService.tryGetCurrentUser();
        return this.artefactsService.getArtefacts(params, user, null);
    }

    @GetMapping("/artefacts/{id}")
    @Operation(summary = "Get the metadata of one artefact.",
            description = "Returns the metadata of the artefact with the given short name, e.g. title, description, version, license, creators, and the hosting provider. " +
                    "If several providers host an artefact with this short name, the response contains one entry per provider; use `database` to choose one.\n\n" +
                    "Example: `GET /artefacts/ncit`")
    public Object getArtefact(@Parameter(description = "Short name of the artefact (`short_form` in `GET /artefacts`)", example = "ncit")
                              @PathVariable("id") String id, @ParameterObject CommonRequestParams params) {
        return this.artefactsService.getArtefact(id, params, null, authService.tryGetCurrentUser());
    }

}
