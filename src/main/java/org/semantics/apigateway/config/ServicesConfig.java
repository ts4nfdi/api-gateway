package org.semantics.apigateway.config;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServicesConfig {
    private List<ServiceConfig> services;

    public ServiceConfig getService(String name) {
        for (ServiceConfig service : services) {
            if (service.getName().equalsIgnoreCase(name)) {
                return service;
            }
        }
        return null;
    }
}



