package org.semantics.apigateway.collections;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.semantics.apigateway.collections.models.TerminologyCollection;
import org.semantics.apigateway.collections.models.TerminologyCollectionDto;
import org.semantics.apigateway.model.user.User;
import org.semantics.apigateway.service.auth.AuthService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/collections")
@Tag(name = "Collections")
@SecurityRequirement(name = "BearerAuth")
@AllArgsConstructor
@CrossOrigin
public class CollectionsController {

    private final CollectionRepository collectionRepository;
    private final AuthService authService;

    @GetMapping("/")
    @Operation(summary = "List all terminology collections you can see.",
            description = "Without login, returns all public terminology collections. " +
                    "When logged in, additionally returns the terminology collections you created or that are shared with you as a collaborator. " +
                    "Each terminology collection contains its `id`, `label`, `description`, `creator`, `collaborators`, the contained `terminologies` " +
                    "(each with `uri`, `label`, `source`, and `type`), and the latest `statistics`. " +
                    "Pass the `id` as `collectionId` to other endpoints to restrict them to the terminologies of the collection.")
    public List<TerminologyCollectionDto> allCollections() {
        User user = authService.tryGetCurrentUser();
        List<TerminologyCollection> collections;
        if(user != null) {
            collections = this.collectionRepository.findAllAccessibleCollections(user);
        } else {
            collections = this.collectionRepository.findByIsPublicTrue();
        }
        return collections.stream().map(TerminologyCollectionDto::toDto).toList();
    }

}