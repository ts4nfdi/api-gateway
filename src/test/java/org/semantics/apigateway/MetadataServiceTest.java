package org.semantics.apigateway;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.semantics.apigateway.config.DatabaseConfig;
import org.semantics.apigateway.model.RDFResource;
import org.semantics.apigateway.model.SemanticArtefact;
import org.semantics.apigateway.model.responses.AggregatedResourceBody;
import org.semantics.apigateway.service.configuration.MetadataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MetadataServiceTest extends ApplicationTestAbstract {

    @Autowired
    private MetadataService metadataService;


    @Test
    public void testGetArtefactMetadata() {
        Map<String, Map<String,String>> out = metadataService.getArtefactMetadata();
        checkFieldsExistence(out, SemanticArtefact.class);
    }

    @Test
    public void testGetTermMetadata() {
        Map<String, Map<String,String>> out = metadataService.getTermMetadata();
        checkFieldsExistence(out, RDFResource.class);
    }

    @Test
    public void testGetSearchMetadata() {
        Map<String, Map<String,String>> out = metadataService.getSearchMetadata();
        checkFieldsExistence(out, RDFResource.class);
    }

    private void checkFieldsExistence(Map<String, Map<String,String>> mappings, Class<? extends AggregatedResourceBody> clazz) {
        List<Field> fields = null;
        try {
            fields = clazz.getDeclaredConstructor().newInstance().getAllFields();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            assert false;
        }

        for (DatabaseConfig config : configurationLoader.getDatabaseConfigs()) {
            Map<String, String> response = mappings.get(config.getDatabase());
            if(response == null) {
                continue;
            }

            fields.forEach(field -> {
                assertThat(response.containsKey(field.getName())).isTrue();
                assertThat(response.get("iri")).isNotNull().isInstanceOf(String.class);
            });
        }
    }
}
