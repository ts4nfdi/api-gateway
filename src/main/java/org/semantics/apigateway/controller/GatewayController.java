package org.semantics.apigateway.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;


@RestController
@RequestMapping("/")
@CrossOrigin
public class GatewayController {

    @GetMapping(path = "/", produces = {MediaType.TEXT_HTML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public void home(
            @RequestHeader(value = "Accept", defaultValue = MediaType.TEXT_HTML_VALUE) String accept,
            HttpServletRequest request, HttpServletResponse response) throws IOException {
        doc(accept, request, response);
    }


    @GetMapping(path = "doc/api", produces = {MediaType.TEXT_HTML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Get API documentation")
    public Object doc(
            @RequestHeader(value = "Accept", defaultValue = MediaType.TEXT_HTML_VALUE) String accept,
            HttpServletRequest request, HttpServletResponse response
    ) throws IOException {
        String redirectUrl;
        if (accept.contains(MediaType.APPLICATION_JSON_VALUE) || accept.contains("application/json+ld")) {
            redirectUrl = request.getContextPath() + "/v3/api-docs";
        } else {
            redirectUrl = request.getContextPath() + "/swagger-ui/index.html?configUrl=/api-gateway/openapi/swagger-config";
        }

        response.setStatus(HttpStatus.MOVED_PERMANENTLY.value());
        response.setHeader("Location", redirectUrl);
        return redirectUrl;
    }


}
