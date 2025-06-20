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

import java.util.concurrent.ExecutionException;


@RestController
@RequestMapping("/")
@Tag(name = "Artefacts / Metadata")
@CrossOrigin
public class ArtefactsController {
    private final ArtefactsService artefactsService;
    private final AuthService authService;

    public ArtefactsController(ArtefactsService artefactsService, AuthService authService) {
        this.artefactsService = artefactsService;
        this.authService = authService;
    }


    @GetMapping("/artefacts")
    @Operation(summary = "Get information about all semantic artefacts.")
    @SecurityRequirement(name = "BearerAuth")
    public Object getArtefacts(@ParameterObject CommonRequestParams params,
                                          @Parameter(description = "Collection id to browse terminologies in") @RequestParam(required = false) String collectionId) throws ExecutionException, InterruptedException {
        User user = authService.tryGetCurrentUser();
        return this.artefactsService.getArtefacts(params, collectionId, user, null);
    }

    @GetMapping("/artefacts/{id}")
    @Operation(summary = "Get information about a semantic artefact.")
    public Object getArtefact(@PathVariable("id") String id, @ParameterObject CommonRequestParams params) {
        return this.artefactsService.getArtefact(id, params, null);
    }

}
