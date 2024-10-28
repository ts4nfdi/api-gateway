package org.semantics.apigateway.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class UriDecodingInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        URI decodedUri = getDecodedURI(request.getURI());

        HttpRequest newRequest = new CustomHttpRequest(decodedUri, request.getMethod(), request.getHeaders());

        return execution.execute(newRequest, body);
    }

    private URI getDecodedURI(URI uri) {
        try {
            // Decode the URI and return a new URI object
            return new URI(URLDecoder.decode(uri.toString(), StandardCharsets.UTF_8));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static class CustomHttpRequest implements ClientHttpRequest {
        private final URI uri;
        private final HttpMethod method;
        private final HttpHeaders headers;

        public CustomHttpRequest(URI uri, HttpMethod method, HttpHeaders headers) {
            this.uri = uri;
            this.method = method;
            this.headers = headers;
        }

        @Override
        public HttpMethod getMethod() {
            return method;
        }

        @Override
        public String getMethodValue() {
            return "";
        }

        @Override
        public URI getURI() {
            return uri;
        }

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }

        @Override
        public ClientHttpResponse execute() throws IOException {
            throw new UnsupportedOperationException("Execute method not implemented");
        }

        @Override
        public OutputStream getBody() throws IOException {
            return null;
        }
    }
}
