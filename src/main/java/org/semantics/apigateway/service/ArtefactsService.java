package org.semantics.apigateway.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.semantics.apigateway.config.DatabaseConfig;
import org.semantics.apigateway.config.OntologyConfig;
import org.semantics.apigateway.model.DynDatabaseTransform;
import org.semantics.apigateway.model.DynTransformResponse;
import org.semantics.apigateway.utils.ApiAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class ArtefactsService {

}
