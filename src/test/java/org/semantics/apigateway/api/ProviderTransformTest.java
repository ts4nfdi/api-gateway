package org.semantics.apigateway.api;

import org.junit.jupiter.api.Test;
import org.semantics.apigateway.config.ResponseMapping;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Covers the aggregator-added provenance fields (backend_type, source, source_name) being
 * carried over into a nested "provider" object by the OLS transformers.
 */
public class ProviderTransformTest {

    private ResponseMapping mapping() {
        ResponseMapping mapping = new ResponseMapping();
        Map<String, String> attributes = new HashMap<>();
        attributes.put("label", "label");
        mapping.setMappedClassAttributes(attributes);
        mapping.setKey("elements");
        return mapping;
    }

    private Map<String, Object> itemWithProvenance() {
        Map<String, Object> item = new HashMap<>();
        item.put("iri", "http://example.org/term/1");
        item.put("label", "Example");
        item.put("backend_type", "ols2");
        item.put("source", "https://www.ebi.ac.uk/ols4/api/v2");
        item.put("source_name", "ebi");
        return item;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> assertProvider(Map<String, Object> transformed) {
        assertThat(transformed).containsKey("provider");
        Map<String, Object> provider = (Map<String, Object>) transformed.get("provider");
        assertThat(provider)
                .containsEntry("provider_type", "ols2")
                .containsEntry("provider_api", "https://www.ebi.ac.uk/ols4/api/v2")
                .containsEntry("provider_name", "ebi");
        return provider;
    }

    @Test
    public void olsV2TransformerNestsProvenanceUnderProvider() {
        Map<String, Object> transformed = new OlsV2Transformer().transformItem(itemWithProvenance(), mapping());
        assertProvider(transformed);
    }

    @Test
    public void olsTransformerNestsProvenanceUnderProvider() {
        Map<String, Object> transformed = new OlsTransformer().transformItem(itemWithProvenance(), mapping());
        assertProvider(transformed);
    }

    @Test
    public void olsV2TransformerOmitsProviderWhenNoProvenance() {
        Map<String, Object> item = new HashMap<>();
        item.put("iri", "http://example.org/term/1");
        item.put("label", "Example");

        Map<String, Object> transformed = new OlsV2Transformer().transformItem(item, mapping());
        assertThat(transformed).doesNotContainKey("provider");
    }

    @Test
    public void olsTransformerOmitsProviderWhenNoProvenance() {
        Map<String, Object> item = new HashMap<>();
        item.put("iri", "http://example.org/term/1");
        item.put("label", "Example");

        Map<String, Object> transformed = new OlsTransformer().transformItem(item, mapping());
        assertThat(transformed).doesNotContainKey("provider");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void olsV2TransformerIncludesOnlyPresentProvenanceFields() {
        Map<String, Object> item = new HashMap<>();
        item.put("iri", "http://example.org/term/1");
        item.put("label", "Example");
        item.put("backend_type", "ols2");

        Map<String, Object> transformed = new OlsV2Transformer().transformItem(item, mapping());
        Map<String, Object> provider = (Map<String, Object>) transformed.get("provider");
        assertThat(provider)
                .containsEntry("provider_type", "ols2")
                .doesNotContainKey("provider_api")
                .doesNotContainKey("provider_name");
    }
}
