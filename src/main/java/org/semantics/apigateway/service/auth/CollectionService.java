package org.semantics.apigateway.service.auth;

import org.semantics.apigateway.model.user.InvalidJwtException;
import org.semantics.apigateway.model.user.TerminologyCollection;
import org.semantics.apigateway.model.user.User;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;


@Service
public class CollectionService  {

    private final CollectionRepository collectionRepository;
    private final AuthService authService;

    public CollectionService(CollectionRepository collectionRepository, AuthService authService) {
        this.collectionRepository = collectionRepository;
        this.authService = authService;
    }


    public Optional<TerminologyCollection> getCollectionById(UUID id, User user) {
        if(user == null) {
            try {
                user = authService.getCurrentUser();
            } catch (Exception ignored) {
                return Optional.empty();
            }
        }
        return collectionRepository.findByUserAndId(user, id);
    }

    public TerminologyCollection getCurrentUserCollection(String collectionId, User user) {

        if (collectionId == null || collectionId.isEmpty()) {
            return null;
        }

        Optional<TerminologyCollection> collection = this.getCollectionById(UUID.fromString(collectionId), user);

        if (collection.isPresent()) {
            return collection.get();
        } else {
            throw new InvalidJwtException("Collection not found for this current user token");
        }
    }

}
