package org.semantics.apigateway.api;

import org.semantics.apigateway.config.ResponseMapping;
import org.semantics.apigateway.model.responses.AggregatedResourceBody;
import org.semantics.apigateway.service.JsonLdTransform;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class OntoPortalTransformer implements DatabaseTransformer {

    private final Map<String, Object> contextConfig;
    private final JsonLdTransform jsonLdTransform;

    public OntoPortalTransformer(Map<String, Object> contextConfig, JsonLdTransform jsonLdTransform) {
        this.contextConfig = contextConfig;
        this.jsonLdTransform = jsonLdTransform;
    }

    @Override
    public Map<String, Object> transformItem(Map<String, Object> item, ResponseMapping mapping) {
        if (item == null) {
            return null;
        }

        Map<String, Object> transformedItem = new LinkedHashMap<>();

        String iri = getStringValue(item, "iri");
        String label = getStringValue(item, "label");
        String type = getStringValue(item, "type");
        String ontology = getStringValue(item, "ontology");
        String source = getStringValue(item, "source");
        String sourceUrl = getStringValue(item, "source_url");

        // Core fields
        if (label != null) {
            transformedItem.put("prefLabel", label);
        }

        Object synonyms = item.get("synonyms");
        if (synonyms != null && !(synonyms instanceof List && ((List<?>) synonyms).isEmpty())) {
            transformedItem.put("synonym", synonyms);
        }

        Object definitions = item.get("descriptions");
        if (definitions != null && !(definitions instanceof List && ((List<?>) definitions).isEmpty())) {
            transformedItem.put("definition", definitions);
        }

        transformedItem.put("obsolete", item.getOrDefault("obsolete", false));

        String matchType = getStringValue(item, "match_type");
        if (matchType != null) {
            transformedItem.put("matchType", matchType);
        }

        String ontologyType = getStringValue(item, "ontology_type");
        if (ontologyType != null) {
            transformedItem.put("ontologyType", ontologyType);
        }

        String sourceName = getStringValue(item, "source_name");
        if(sourceName != null){
            transformedItem.put("source", sourceName);
        }

            if (iri != null) {
            transformedItem.put("@id", iri);
        }

        if (type != null) {
            transformedItem.put("@type", type);
        }

        // Build links
        if (source != null && ontology != null && iri != null) {
            String encodedIri = URLEncoder.encode(iri, StandardCharsets.UTF_8);
            String ontologyAcronym = ontology.contains("/") ? ontology.substring(ontology.lastIndexOf('/') + 1) : ontology;
            String selfUrl = source + "/ontologies/" + ontologyAcronym + "/classes/" + encodedIri;
            String ontologyUrl = source + "/ontologies/" + ontologyAcronym;

            Map<String, Object> links = new LinkedHashMap<>();
            links.put("self", selfUrl);
            links.put("ontology", ontologyUrl);
            links.put("children", selfUrl + "/children");
            links.put("parents", selfUrl + "/parents");
            links.put("descendants", selfUrl + "/descendants");
            links.put("ancestors", selfUrl + "/ancestors");
            links.put("instances", selfUrl + "/instances");
            links.put("tree", selfUrl + "/tree");
            links.put("notes", selfUrl + "/notes");
            links.put("mappings", selfUrl + "/mappings");

            if (sourceUrl != null) {
                links.put("ui", sourceUrl);
            }

            // Links @context from YAML config
            Map<String, Object> linksContext = buildLinksContext();
            links.put("@context", linksContext);

            transformedItem.put("links", links);
        }

        // Item-level @context generated from annotations
        Map<String, Object> context = buildItemContext();
        transformedItem.put("@context", context);

        return transformedItem;
    }

    @Override
    public Map<String, Object> constructResponse(List<Map<String, Object>> transformedResults, String mappingKey, boolean list, boolean paginate, int page, long totalCount) {
        Map<String, Object> response = new LinkedHashMap<>();

        response.put("totalCount", totalCount > 0 ? totalCount : transformedResults.size());
        response.put("collection", transformedResults);

        return response;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> buildItemContext() {
        Map<String, Object> context = new LinkedHashMap<>();

        String vocab = contextConfig != null ? (String) contextConfig.get("vocab") : null;
        String language = contextConfig != null ? (String) contextConfig.get("language") : null;
        Map<String, String> fieldMappings = contextConfig != null ? (Map<String, String>) contextConfig.get("fieldMappings") : null;
        Map<String, String> fieldOverrides = contextConfig != null ? (Map<String, String>) contextConfig.get("fieldOverrides") : null;

        if (vocab != null) {
            context.put("@vocab", vocab);
        }

        // Generate context from @ContextUri annotations
        if (fieldMappings != null && jsonLdTransform != null) {
            Map<String, String> generatedContext = jsonLdTransform.generateContext(
                    org.semantics.apigateway.model.RDFResource.class, null);

            for (Map.Entry<String, String> entry : fieldMappings.entrySet()) {
                String ontoPortalKey = entry.getKey();    // e.g. "prefLabel"
                String javaFieldName = entry.getValue();   // e.g. "label"

                // Check for override first
                if (fieldOverrides != null && fieldOverrides.containsKey(ontoPortalKey)) {
                    context.put(ontoPortalKey, fieldOverrides.get(ontoPortalKey));
                } else if (generatedContext.containsKey(javaFieldName)) {
                    context.put(ontoPortalKey, generatedContext.get(javaFieldName));
                }
            }
        }

        if (language != null) {
            context.put("@language", language);
        }

        return context;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> buildLinksContext() {
        if (contextConfig == null) {
            return new LinkedHashMap<>();
        }
        Map<String, String> linksConfig = (Map<String, String>) contextConfig.get("links");
        if (linksConfig == null) {
            return new LinkedHashMap<>();
        }
        return new LinkedHashMap<>(linksConfig);
    }

    private String getStringValue(Map<String, Object> item, String key) {
        Object value = item.get(key);
        return value != null ? value.toString() : null;
    }
}
