package org.semantics.apigateway.repository;

import org.semantics.apigateway.model.terminology.TerminologyIdUriMap;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TerminologyIdUriMapRepository extends JpaRepository<TerminologyIdUriMap, Long> {
    Optional<TerminologyIdUriMap> findByTerminologyId(String terminologyId);
}
