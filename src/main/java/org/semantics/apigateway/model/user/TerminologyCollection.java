package org.semantics.apigateway.model.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
public class TerminologyCollection {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(length = 500)
    private String description;

    @Column()
    private String label;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> terminologies;

}
