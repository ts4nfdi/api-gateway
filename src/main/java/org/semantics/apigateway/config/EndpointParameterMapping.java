package org.semantics.apigateway.config;

import lombok.*;

import java.util.Arrays;
import java.util.function.Function;

@Getter
@Setter
@NoArgsConstructor
public class EndpointParameterMapping {
  
  @AllArgsConstructor
  public static class Parameter {
    private String name;
    private String value;
  }
  
  @AllArgsConstructor
  public enum Type {
    path(x -> x.value),
    string(x -> x.name + "=" + x.value),
    stringAsterisk(x -> x.name + "=" + x.value + "*"),
    integer(x -> x.name + "=" + x.value),
    commaList(x -> x.name + "=" + String.join(",", x.value.split(","))),
    pipeList(x -> x.name + "=" + String.join("|", x.value.split(","))),
    jsonList(x -> x.name + "=" + "[" + String.join(",", x.value.split(",")) + "]"),
    repeatedKeyList(x -> String.join("&", Arrays.stream(x.value.split(",")).sequential().map(s -> x.name + "=" + s).toList()));
    
    private Function<Parameter, String> serializer;
    
  }
  
  @NonNull
  private Type type;
  
  private String name;
  private boolean optional = false;
  
  public String serializeParameter(String value) {
    return type.serializer.apply(new Parameter(name, value));
  }
  
}
