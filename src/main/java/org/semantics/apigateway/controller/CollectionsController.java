package org.semantics.apigateway.controller;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.semantics.apigateway.model.user.TerminologyCollection;
import org.semantics.apigateway.service.auth.AuthService;
import org.semantics.apigateway.service.auth.CollectionRepository;
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
public class CollectionsController {

    private final CollectionRepository collectionRepository;
    private final AuthService authService;


    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/")
    public TerminologyCollection createCollection(@RequestBody TerminologyCollection collection) {
        collection.setUser(authService.getCurrentUser());
        this.collectionRepository.save(collection);
        return collection;
    }

    @PutMapping("/{id}")
    public TerminologyCollection updateCollection(@PathVariable UUID id, @RequestBody TerminologyCollection collection) {
        TerminologyCollection terminologyCollection = collectionRepository.findByUserAndId(authService.getCurrentUser(), id).orElse(null);
        assert terminologyCollection != null;
        terminologyCollection.setTerminologies(collection.getTerminologies());
        terminologyCollection.setLabel(collection.getLabel());
        terminologyCollection.setDescription(collection.getDescription());
        this.collectionRepository.save(terminologyCollection);
        return terminologyCollection;
    }

    @GetMapping("/")
    public List<TerminologyCollection> allCollections() {
        return this.collectionRepository.findByUser(authService.getCurrentUser());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCollection(@PathVariable UUID id) {
        TerminologyCollection collection = this.collectionRepository.
                findByUserAndId(authService.getCurrentUser(), id).orElse(null);
        assert collection != null;
        this.collectionRepository.delete(collection);
    }
}
