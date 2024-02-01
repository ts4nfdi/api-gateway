package org.semantics.nfdi.model;

import com.github.jsonldjava.utils.JsonUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.semantics.nfdi.config.OntologyConfig;
import org.semantics.nfdi.config.ResponseMapping;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JsonLdTransform {

    public static List<Map<String, Object>> convertToJsonLd(List<Map<String, Object>> response, OntologyConfig config) {
        Map<String, Object> context = new HashMap<>();
        context.put("@vocab", "http://base4nfdi.de/ts4nfdi/schema/");
        context.put("ts", "http://base4nfdi.de/ts4nfdi/schema/");
        String type = "ts:Resource";

        ResponseMapping responseMapping = config.getResponseMapping();

        return response.stream().map(item -> {
            try {
                Map<String, Object> jsonLd = new HashMap<>();
                jsonLd.put("@context", context);
                jsonLd.put("@type", type);
                for (Map.Entry<String, Object> entry : item.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();

                    if (responseMapping.containsKey(key)) {
                        key = responseMapping.get(key);
                    }

                    jsonLd.put(key, value);
                }
                String jsonString = JsonUtils.toString(jsonLd);
                if (jsonString != null) {
                    String jsonLdString = convertJsonToJsonLd(jsonString);
                    return (Map<String, Object>) JsonUtils.fromString(jsonLdString);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        }).collect(Collectors.toList());
    }

    public static String convertJsonToJsonLd(String json) {
        Model model = ModelFactory.createDefaultModel();
        RDFDataMgr.read(model, new StringReader(json), null, Lang.JSONLD);

        StringWriter out = new StringWriter();
        RDFDataMgr.write(out, model, Lang.JSONLD);
        return out.toString();
    }
}