package org.semantics.apigateway;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.semantics.apigateway.model.user.Role;
import org.semantics.apigateway.model.user.User;
import org.semantics.apigateway.service.auth.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.concurrent.Executor;

@SpringBootApplication
@EnableConfigurationProperties
@EnableAsync
@EnableCaching
@OpenAPIDefinition(
        info = @Info(title = "API Gateway Documentation", version = "1.0", description = "The TS4NFDI Federated Service is an advanced, dynamic solution designed to perform federated calls across multiple Terminology Services (TS) within NFDI. It is particularly tailored for environments where integration and aggregation of diverse data sources are essential. The service offers search capabilities, enabling users to refine search results based on specific criteria, and supports responses in both JSON and JSON-LD formats.\n" +
                "\n" +
                "A standout feature of this service is its dynamic nature, governed by a JSON configuration file. This design choice allows for easy extension and customization of the service to include new TS or modify existing configurations."),
        tags = {
                @Tag(name = "Search"),
                @Tag(name = "Artefacts"),
        }
)
public class APIGatewayApplication implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(APIGatewayApplication.class);
    private UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${user.admin.password}")
    private String password;

    public APIGatewayApplication(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public static void main(String[] args) {

        SpringApplication.run(APIGatewayApplication.class, args);
    }

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("Async-");
        executor.initialize();
        return executor;
    }

    @Override
    public void run(String... args) throws Exception {
        this.userRepository.findByUsername("admin").ifPresentOrElse(
                user -> logger.info("Admin user already exists"),
                () -> {
                    logger.info("Creating admin user");
                    User admin = new User();
                    admin.setUsername("admin");
                    admin.setPassword(passwordEncoder.encode(password));
                    admin.setRoles(Collections.singleton(Role.ADMIN));
                    this.userRepository.save(admin);
                    logger.info("Admin user created");
                }
        );
    }
}
