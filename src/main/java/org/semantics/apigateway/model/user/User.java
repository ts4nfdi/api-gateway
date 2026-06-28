package org.semantics.apigateway.model.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.util.Set;


@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @Positive
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @NonNull
    @NotEmpty
    private String username;

    @Column(nullable = false)
    @NonNull
    @NotEmpty
    @JsonIgnore
    private String password;

    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Role> roles;
    
    // TODO at some point, we will want this to be @NotEmpty and @Column(nullable = false), but for the period of transitioning to OIDC, we will need to allow users that are not associated with an OIDC identity yet.
    @Column(unique = true)
    private String oidcSubjectIdentifier;
}
