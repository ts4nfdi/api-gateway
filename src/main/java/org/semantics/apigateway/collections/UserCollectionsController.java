package org.semantics.apigateway.collections;

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
@Tag(name = "Users - Collections")
@SecurityRequirement(name = "BearerAuth")
@AllArgsConstructor
@CrossOrigin
public class UserCollectionsController {

    private final CollectionRepository collectionRepository;
    private final CollectionService collectionService;
    private final AuthService authService;


    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/")
    public TerminologyCollectionDto createCollection(@RequestBody TerminologyCollectionDto collection) {

        return TerminologyCollectionDto.toDto(collectionService.createCollection(authService.getCurrentUser().getId(), collection));
    }

    @PutMapping("/{id}")
    public TerminologyCollectionDto updateCollection(@PathVariable UUID id, @RequestBody TerminologyCollectionDto collection) {
        TerminologyCollection existingCollection = this.collectionRepository.findByIdAndWritable(id, authService.getCurrentUser()).orElse(null);
        if (existingCollection == null) {
            throw new NotFoundException("Collection not found");
        }
        return TerminologyCollectionDto.toDto(this.collectionService.updateCollection(authService.getCurrentUser(), id, collection));
    }

    @GetMapping("/")
    public List<TerminologyCollectionDto> allCollections() {
        List<TerminologyCollection> collections = this.collectionRepository.findAllUserCollections(authService.getCurrentUser());
        return collections.stream().map(TerminologyCollectionDto::toDto).toList();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCollection(@PathVariable UUID id) {
        this.collectionService.deleteCollection(authService.getCurrentUser(), id);
    }
}