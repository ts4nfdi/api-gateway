package org.semantics.apigateway.model;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.QueryParam;
import lombok.Data;

@Data
public class PaginationParams {
  
  @QueryParam("page")
  @Parameter(name = "page", in = ParameterIn.QUERY, required = false,
          description =
                  "The page number to be retrieved.",
          schema = @Schema(type = "integer", format = "int64", minimum = "1", defaultValue = "1", example = "1"))
  @Min(1) private int page = 1;
  
  @QueryParam("pagesize")
  @Parameter(name = "pagesize", in = ParameterIn.QUERY, required = false,
          description =
                  "The number of items returned per page.",
          schema = @Schema(type = "integer", format = "int64", minimum = "1", defaultValue = "50", example = "50"))
  @Min(1) private int pagesize = 50;
}
