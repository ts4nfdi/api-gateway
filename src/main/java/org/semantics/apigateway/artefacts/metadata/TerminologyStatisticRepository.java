package org.semantics.apigateway.artefacts.metadata;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface TerminologyStatisticRepository extends JpaRepository<TerminologyStatistic, Long> {

    @Query("SELECT s FROM TerminologyStatistic s WHERE s.timestamp = " +
            "(SELECT MAX(t.timestamp) FROM TerminologyStatistic t WHERE t.uri = s.uri AND t.source = s.source)")
    List<TerminologyStatistic> findLatest();
}
