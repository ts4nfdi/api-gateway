package org.semantics.apigateway.collections;

import org.semantics.apigateway.collections.models.TerminologyCollection;
import org.semantics.apigateway.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface CollectionRepository extends JpaRepository<TerminologyCollection, Long> {
    List<TerminologyCollection> findByUser(User user);

    Optional<TerminologyCollection> findByUserAndId(User user, UUID id);


    List<TerminologyCollection> findByIsPublicTrue();

    @Query("SELECT DISTINCT t FROM TerminologyCollection t " +
            "LEFT JOIN t.collaborators c " +
            "WHERE t.isPublic = true " +
            "OR t.user = :user " +
            "OR c.user = :user")
    List<TerminologyCollection> findAllAccessibleCollections(User user);

    @Query("SELECT DISTINCT t FROM TerminologyCollection t " +
            "LEFT JOIN t.collaborators c " +
            "WHERE t.user = :user " +
            "OR c.user = :user")
    List<TerminologyCollection> findAllUserCollections(User user);

    @Query("SELECT DISTINCT t FROM TerminologyCollection t " +
            "LEFT JOIN t.collaborators c " +
            "WHERE t.id = :id AND (t.isPublic = true OR t.user = :user OR c.user = :user)")
    Optional<TerminologyCollection> findByIdAccessible(UUID id, User user);

    @Query("SELECT DISTINCT t FROM TerminologyCollection t " +
            "LEFT JOIN t.collaborators c " +
            "WHERE t.id = :id AND (t.user = :user OR c.user = :user)")
    Optional<TerminologyCollection> findByIdAndWritable(UUID id, User user);


    Optional<TerminologyCollection> findByIdAndIsPublicTrue(UUID id);
}
