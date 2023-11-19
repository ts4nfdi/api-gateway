package org.semantics.nfdi.config;


import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OntologyConfig {
    private String ontology;
    private String url;
    private String apiKey;
    private ResponseMapping responseMapping;
}
