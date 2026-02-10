package org.semantics.apigateway;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@ComponentScan("org.semantics.apigateway.controller.ols")
@EnableAspectJAutoProxy
public class AspectConfig {

}
