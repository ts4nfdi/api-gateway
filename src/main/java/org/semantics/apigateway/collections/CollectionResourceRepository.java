package org.semantics.apigateway.collections;

import org.semantics.apigateway.collections.models.CollectionResource;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CollectionResourceRepository extends JpaRepository<CollectionResource, Long> {
}
