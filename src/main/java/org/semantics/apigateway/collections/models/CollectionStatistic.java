package org.semantics.apigateway.collections.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

/**
 * Statistics of a terminology collection. Rows are written only by the external statistics script.
 */
@Entity
@Immutable
@Table(name = "collection_statistics")
@Getter
@Setter
public class CollectionStatistic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id")
    @JsonIgnore
    private TerminologyCollection collection;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    private long propertyCount;
    private long classCount;
    private long namedIndividualCount;
    private long conceptCount;
}