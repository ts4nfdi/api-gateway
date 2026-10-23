package org.semantics.apigateway.collections;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.NotFoundException;
import lombok.AllArgsConstructor;
import org.semantics.apigateway.collections.models.TerminologyCollection;
import org.semantics.apigateway.collections.models.TerminologyCollectionDto;
import org.semantics.apigateway.model.user.User;
import org.semantics.apigateway.service.auth.AuthService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/collections")
@Tag(name = "Users - Collections")
@SecurityRequirement(name = "BearerAuth")
@AllArgsConstructor
@CrossOrigin
public class CollectionsController {

    private final CollectionRepository collectionRepository;
    private final CollectionService collectionService;
    private final AuthService authService;

    @GetMapping("/")
    @Operation(summary = "List collections")
    @Parameter(name = "id", in = ParameterIn.QUERY, required = false,
            description = "If given, returns the single collection with this id instead of the list")
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

    @Operation(hidden = true)
    @GetMapping(value = {"", "/"}, params = "id")
    public TerminologyCollectionDto getCollectionById(@RequestParam String id) {
        return resolveCollection(id);
    }

    @Operation(
            summary = "Get a collection by id or permalink",
            description = "Returns a single terminology collection. The `id` can be either the collection's UUID "
                    + "or a full permalink URL ending in the UUID. Public collections are accessible without "
                    + "authentication; private collections are only returned if the authenticated user has access."
    )
    @GetMapping("/{id}")
    public TerminologyCollectionDto getCollectionByPathId(
            @Parameter(description = "Collection UUID or permalink ending in the UUID",
                    example = "https://w3id.org/ts4nfdi/collection/ff5491d1-d0a9-481e-ac90-0fad065fa097")
            @PathVariable String id) {
        return resolveCollection(id);
    }

    private TerminologyCollectionDto resolveCollection(String id) {
        UUID collectionId;
        try {
            collectionId = UUID.fromString(id.substring(id.lastIndexOf('/') + 1));
        } catch (IllegalArgumentException e) {
            throw new NotFoundException("Invalid collection id or permalink");
        }
        User user = authService.tryGetCurrentUser();
        TerminologyCollection collection = collectionService.getCollectionById(collectionId, user)
                .orElseThrow(() -> new NotFoundException("Collection not found"));
        return TerminologyCollectionDto.toDto(collection);
    }

}