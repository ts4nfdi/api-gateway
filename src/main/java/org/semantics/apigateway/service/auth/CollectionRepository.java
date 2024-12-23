package org.semantics.apigateway.service.auth;

import org.semantics.apigateway.model.user.TerminologyCollection;
import org.semantics.apigateway.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface CollectionRepository extends JpaRepository<TerminologyCollection, Long> {
    List<TerminologyCollection> findByUser(User user);

    Optional<TerminologyCollection> findByUserAndId(User user, UUID id);
}
