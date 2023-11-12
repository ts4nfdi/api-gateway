package org.semantics.nfdi.config;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OntologyConfig {
    private String name;
    private List<String> fields;
    private String url;
}
