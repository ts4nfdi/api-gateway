package org.semantics.apigateway.config;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Component
public class EndpointConfig {
    private String path;
    private Map<String, String> responseMapping;
    private boolean caseInsensitive = true;
}
