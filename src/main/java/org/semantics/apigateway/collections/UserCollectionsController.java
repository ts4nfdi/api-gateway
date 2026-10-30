package org.semantics.apigateway.collections;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.NotFoundException;
import lombok.AllArgsConstructor;
import org.semantics.apigateway.collections.models.TerminologyCollection;
import org.semantics.apigateway.collections.models.TerminologyCollectionDto;
import org.semantics.apigateway.service.auth.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users/collections")
@Tag(name = "Collections")
@SecurityRequirement(name = "BearerAuth")
@AllArgsConstructor
@CrossOrigin
public class UserCollectionsController {

    private final CollectionRepository collectionRepository;
    private final CollectionService collectionService;
    private final AuthService authService;


    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/")
    @Operation(summary = "Create a terminology collection.",
            description = "Creates a new terminology collection owned by the logged-in user and returns it, including its generated `id`. " +
                    "Required fields: `label`, `description`, `isPublic` (whether everybody can see and use it). " +
                    "Optional: `terminologies`, the terminologies to include (each with the artefact's `uri`, its `label`, and the `source` hosting it), " +
                    "and `collaborators`, the usernames of other users who may edit the collection. Requires login.")
    public TerminologyCollectionDto createCollection(@RequestBody TerminologyCollectionDto collection) {

        return TerminologyCollectionDto.toDto(collectionService.createCollection(authService.getCurrentUser().getId(), collection));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a terminology collection.",
            description = "Replaces label, description, visibility, terminologies, and collaborators of the terminology collection with the values in the request body, " +
                    "and returns the updated collection. Only the creator and the collaborators of the collection may update it. Requires login.")
    public TerminologyCollectionDto updateCollection(@Parameter(description = "`id` of the terminology collection") @PathVariable UUID id, @RequestBody TerminologyCollectionDto collection) {
        TerminologyCollection existingCollection = this.collectionRepository.findByIdAndWritable(id, authService.getCurrentUser()).orElse(null);
        if (existingCollection == null) {
            throw new NotFoundException("Collection not found");
        }
        return TerminologyCollectionDto.toDto(this.collectionService.updateCollection(authService.getCurrentUser(), id, collection));
    }

    @GetMapping("/")
    @Operation(summary = "List your own terminology collections.",
            description = "Returns the terminology collections that the logged-in user created or is a collaborator of, both public and private. " +
                    "Unlike `GET /collections/`, public collections of other users are not included. Requires login.")
    public List<TerminologyCollectionDto> allCollections() {
        List<TerminologyCollection> collections = this.collectionRepository.findAllUserCollections(authService.getCurrentUser());
        return collections.stream().map(TerminologyCollectionDto::toDto).toList();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a terminology collection.",
            description = "Deletes the terminology collection permanently. Only the creator and the collaborators of the collection may delete it. " +
                    "The terminologies themselves are not affected. Requires login.")
    public void deleteCollection(@Parameter(description = "`id` of the terminology collection") @PathVariable UUID id) {
        this.collectionService.deleteCollection(authService.getCurrentUser(), id);
    }
}