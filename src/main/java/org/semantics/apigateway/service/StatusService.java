package org.semantics.apigateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Getter
@Service
public class StatusService {
    private Map<String, Map<String, String>> allExamples = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(StatusService.class);
    private final WebClient webClient;

    public StatusService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Setter
    @Getter
    public static class StatusResult {
        private double percentageCommon = 0.0;
        private double percentageFilled = 0.0;
        private int totalOriginalKeys = 0;
        private int totalMainKeys = 0;
        private List<String> emptyKeys = new ArrayList<>();
        private List<String> commonKeys = new ArrayList<>();
    }

    public String getBaseUrl(HttpServletRequest request) {
        return ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(request.getContextPath())
                .replaceQuery(null)
                .toUriString();
    }

    public Map<?, ?> getResultFromUrlReactive(String url) {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();

        return webClient.get()
                .uri(getBaseUrl(request) + url)
                .retrieve()
                .bodyToMono(String.class)
                .<Map<?, ?>>handle((json, sink) -> {
                    try {
                        sink.next(new ObjectMapper().readValue(json, Map.class));
                    } catch (Exception e) {
                        sink.error(new RuntimeException("Failed to parse JSON", e));
                    }
                }).block();
    }

    @PostConstruct
    public void init() {
        loadExamplesFromYaml();
    }

    private void loadExamplesFromYaml() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("examples.yaml")) {
            if (inputStream == null) {
                throw new IOException("Could not find examples.yaml");
            }

            Yaml yaml = new Yaml();
            allExamples = yaml.load(inputStream);

        } catch (IOException e) {
            logger.error("Error loading YAML file: {}", e.getMessage());
        }
    }

    public Map<String, Object>  checkEndpoint(String endpoint) {
        endpoint = URLDecoder.decode(endpoint, StandardCharsets.UTF_8);
        String url = UriComponentsBuilder.fromUriString(endpoint)
                .queryParam("showResponseConfiguration", "true")
                .toUriString();

        return calculateGlobalStats(url);
    }

    private Map<String, Object> calculateGlobalStats(String exampleUrl) {
        Map<?, ?> apiResponse = getResultFromUrlReactive(exampleUrl);

        List<Map<String, Object>> data = (List<Map<String, Object>>) apiResponse.get("collection");

        if (data == null || apiResponse.get("responseConfig") == null) {
            ;
            return null;
        }
        Map<String, Object> responseConfig = (Map<String, Object>) apiResponse.get("responseConfig");
        responseConfig.put("endpoint", exampleUrl);
        return responseConfig;
    }

}