package org.semantics.apigateway.service;

import jakarta.websocket.OnClose;
import org.semantics.apigateway.model.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.semantics.apigateway.model.terminology.TerminologyIdUriMap;
import org.semantics.apigateway.repository.TerminologyIdUriMapRepository;
import org.semantics.apigateway.service.configuration.ConfigurationLoader;
import org.semantics.apigateway.config.DatabaseConfig;
import org.semantics.apigateway.config.ServiceConfig;
import org.springframework.web.client.RestTemplate;
import org.semantics.apigateway.service.ResponseTransformerService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;




import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@Service
public class TerminologyUriFetcher {

    @Autowired
    private TerminologyIdUriMapRepository idUriMapRepository;

    @Autowired
    private ConfigurationLoader configLoader;

    private ResponseTransformerService respTransformer = new ResponseTransformerService(configLoader);


    @Scheduled(fixedRate = 50000)
    public void run() throws Exception{

        List<DatabaseConfig> dbconfigs = configLoader.getDatabaseConfigs();
        List<ServiceConfig> servConfigs = configLoader.getServiceConfigs();
        RestTemplate client = new RestTemplate();
        for(DatabaseConfig db: dbconfigs){
            for(ServiceConfig sc:servConfigs){
                if(!sc.getName().equals("ols2")){
                    continue;
                }
                if(db.getType().equals(sc.getName())){
                    String url = db.getUrl() + "/" + sc.getEndpoints().get("resources").getPath();
                    String resp = client.getForObject(url, String.class);
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode root = mapper.readTree(resp);
                    if(root.has("elements")){
                        // ols4 v2
                        for(JsonNode onto: root.path("elements")){
                            if(!onto.has("ontologyId") || !onto.has("iri")){
                                continue;
                            }
                            TerminologyIdUriMap tIdUriMap = new TerminologyIdUriMap();
                            tIdUriMap.setTerminologyId(onto.path("ontologyId").asText());
                            tIdUriMap.setSource(db.getName());
                            tIdUriMap.setUri(onto.path("iri").asText());
                            // change to check the source (db) as well
                            Optional<TerminologyIdUriMap> record = this.idUriMapRepository.findByTerminologyId(tIdUriMap.getTerminologyId());
                            if(!record.isPresent()){
                                System.out.println("storing: " + onto.path("ontologyId"));
                                this.idUriMapRepository.save(tIdUriMap);
                            }
                        }
                    }
               }
            }
        }

        System.out.println("Scheduled job ran successfully...");
    }

}
