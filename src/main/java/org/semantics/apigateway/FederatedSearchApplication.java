package org.semantics.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableConfigurationProperties
public class FederatedSearchApplication {

	public static void main(String[] args) {

		SpringApplication.run(FederatedSearchApplication.class, args);
	}
}
