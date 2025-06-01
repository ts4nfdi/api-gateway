package org.semantics.apigateway.collections;

import jakarta.ws.rs.NotFoundException;
import org.semantics.apigateway.collections.models.*;
import org.semantics.apigateway.model.user.InvalidJwtException;
import org.semantics.apigateway.model.user.User;
import org.semantics.apigateway.service.auth.AuthService;
import org.semantics.apigateway.service.auth.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
public class CollectionService  {

    private final CollectionRepository collectionRepository;
    private final AuthService authService;
    private final UserRepository userRepository;
    private final CollectionResourceRepository collectionResourceRepository;
    private final CollectionCollaboratorRepository collectionCollaboratorRepository;

    public CollectionService(CollectionRepository collectionRepository, AuthService authService, UserRepository userRepository, CollectionResourceRepository collectionResourceRepository, CollectionCollaboratorRepository collectionCollaboratorRepository) {
        this.collectionRepository = collectionRepository;
        this.authService = authService;
        this.userRepository = userRepository;
        this.collectionResourceRepository = collectionResourceRepository;
        this.collectionCollaboratorRepository = collectionCollaboratorRepository;
    }


    public Optional<TerminologyCollection> getCollectionById(UUID id, User user) {
        if(user == null) {
            return  collectionRepository.findByIdAndIsPublicTrue(id);
        } else {
            return collectionRepository.findByIdAccessible(id, user);
        }
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

    public TerminologyCollection updateCollection(User user, UUID collectionId, TerminologyCollectionDto dto) {
        TerminologyCollection terminologyCollection = collectionRepository.findByIdAccessible(collectionId, user)
                .orElseThrow(() -> new NotFoundException("Collection not found"));

        terminologyCollection.getTerminologies().clear();
        terminologyCollection.getCollaborators().clear();

        List<CollectionCollaborator> collectionCollaborators = createCollectionCollaborators(dto.getCollaborators(), terminologyCollection);
        List<CollectionResource> resources = createCollectionResources(dto.getTerminologies(), terminologyCollection);

        terminologyCollection.setLabel(dto.getLabel());
        terminologyCollection.setDescription(dto.getDescription());
        terminologyCollection.setPublic(dto.isPublic());

        terminologyCollection.getTerminologies().addAll(resources);
        terminologyCollection.getCollaborators().addAll(collectionCollaborators);

        return collectionRepository.save(terminologyCollection);
    }

    public TerminologyCollection createCollection(long userId, TerminologyCollectionDto dto) {
        User owner = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));



        TerminologyCollection collection = new TerminologyCollection();
        collection.setUser(owner);
        collection.setLabel(dto.getLabel());
        collection.setDescription(dto.getDescription());
        collection.setPublic(dto.isPublic());
        TerminologyCollection savedCollection = collectionRepository.save(collection);

        List<CollectionCollaborator> collectionCollaborators = createCollectionCollaborators(dto.getCollaborators(), savedCollection);
        List<CollectionResource> resources = createCollectionResources(dto.getTerminologies(), savedCollection);

        savedCollection.setTerminologies(resources);
        savedCollection.setCollaborators(collectionCollaborators);
        return savedCollection;
    }

    public void deleteCollection(User user, UUID id) {
        TerminologyCollection collection = this.collectionRepository.
                findByIdAndWritable(id, user).orElse(null);
        if (collection == null) {
            throw new NotFoundException("Collection not found");
        }
        this.collectionRepository.delete(collection);
    }


    private List<CollectionResource> createCollectionResources(List<ResourceDto> resources, TerminologyCollection collection) {
        List<CollectionResource> collectionResources = resources.stream().map(resource -> {
            CollectionResource collectionResource = new CollectionResource();
            collectionResource.setUri(resource.uri());
            collectionResource.setLabel(resource.label());
            collectionResource.setSource(resource.source());
            collectionResource.setType(resource.type());
            collectionResource.setCollection(collection);
            return collectionResource;
        }).toList();
        return  collectionResourceRepository.saveAll(collectionResources);
    }
    private List<CollectionCollaborator> createCollectionCollaborators(List<CollectionCollaboratorDto> usernames, TerminologyCollection collection) {
        List<User> collaborators = userRepository.findByUsernameIn(usernames.stream().map(CollectionCollaboratorDto::username).toList());
        List<CollectionCollaborator> collectionCollaborators =  collaborators.stream().map(user -> {
            CollectionCollaborator collectionCollaborator = new CollectionCollaborator();
            collectionCollaborator.setUser(user);
            collectionCollaborator.setCollection(collection);
            return collectionCollaborator;
        }).toList();
        return  collectionCollaboratorRepository.saveAll(collectionCollaborators);
    }
}