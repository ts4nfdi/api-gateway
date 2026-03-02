package org.semantics.apigateway.controller.ols;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.semantics.apigateway.model.CommonRequestParams;
import org.semantics.apigateway.model.TargetDbSchema;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class OlsTargetDbSchemaAspect {
  
  @Pointcut("within(org.semantics.apigateway.controller.ols.*) && @annotation(org.springframework.web.bind.annotation.GetMapping) && execution(public * *(.., org.semantics.apigateway.model.CommonRequestParams, ..))")
  public void olsEndpoint() {}
  
  @Pointcut("olsEndpoint() && execution(public * org.semantics.apigateway.controller.ols.Ols3Controller.*(..))")
  public void ols3Endpoint() {}
  
  @Pointcut("olsEndpoint() && execution(public * org.semantics.apigateway.controller.ols.Ols4Controller.*(..))")
  public void ols4Endpoint() {}
  
  @Before("ols3Endpoint()")
  public void ols3EndpointBefore(JoinPoint joinPoint) {
    setTargetDbSchema(joinPoint, TargetDbSchema.ols);
  }
  
  @Before("ols4Endpoint() ")
  public void ols4EndpointBefore(JoinPoint joinPoint) {
    setTargetDbSchema(joinPoint, TargetDbSchema.ols2);
  }
  
  private void setTargetDbSchema(JoinPoint joinPoint, TargetDbSchema targetDbSchema) {
    Arrays.stream(joinPoint.getArgs()).filter(arg -> arg instanceof CommonRequestParams).findFirst().ifPresent(params -> ((CommonRequestParams)params).setTargetDbSchema(targetDbSchema));
  }
}
