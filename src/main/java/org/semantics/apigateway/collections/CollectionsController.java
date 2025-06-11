package org.semantics.apigateway.collections;

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
@Tag(name = "Users - Collections")
@SecurityRequirement(name = "BearerAuth")
@AllArgsConstructor
@CrossOrigin
public class CollectionsController {

    private final CollectionRepository collectionRepository;
    private final AuthService authService;

    @GetMapping("/")
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