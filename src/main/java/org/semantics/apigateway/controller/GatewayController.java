package org.semantics.apigateway.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.apache.http.HttpStatus;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.lucene.queryparser.classic.ParseException;
import org.semantics.apigateway.service.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;


@RestController
@RequestMapping("/")
public class GatewayController {

    @CrossOrigin
    @GetMapping("/")
    public void home(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/swagger-ui/index.html?configUrl=/api-gateway/openapi/swagger-config");
    }



}
