package org.semantics.apigateway.model.terminology;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import  lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "TerminologiesIdUri")
public class TerminologyIdUriMap {
    @Id
    @Positive
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @Column(nullable = false, unique = true)
    @NotEmpty
    @NotNull
    private String terminologyId;

    @Column(nullable = false, unique = true)
    @NotEmpty
    @NotNull
    private String uri;
}
