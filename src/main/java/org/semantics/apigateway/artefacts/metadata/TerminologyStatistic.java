package org.semantics.apigateway.artefacts.metadata;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

/**
 * Statistics of a terminology. Rows are written only by the external statistics script.
 */
@Entity
@Immutable
@Table(name = "terminology_statistics")
@Getter
public class TerminologyStatistic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String uri;

    @Column(nullable = false)
    private String source;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    private long propertyCount;
    private long classCount;
    private long namedIndividualCount;
    private long conceptCount;
}
