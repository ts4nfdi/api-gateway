package org.semantics.apigateway.service;

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

import java.util.Optional;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class TerminologyUriFetcher {

    @Autowired
    private TerminologyIdUriMapRepository idUriMapRepository;

    @Autowired
    private ConfigurationLoader configLoader;


    @Scheduled(fixedRate = 10000)
    public void run(){
        TerminologyIdUriMap tIdUriMap = new TerminologyIdUriMap();
        tIdUriMap.setTerminologyId("vibso");
        tIdUriMap.setSource("tib");
        tIdUriMap.setUri("http://purl.obolibrary.org/obo/vibso.owl");
        Optional<TerminologyIdUriMap> record = this.idUriMapRepository.findByTerminologyId(tIdUriMap.getTerminologyId());
        if(!record.isPresent()){
            this.idUriMapRepository.save(tIdUriMap);
        }
        List<DatabaseConfig> dbconfigs = configLoader.getDatabaseConfigs();
        List<ServiceConfig> servConfigs = configLoader.getServiceConfigs();
        RestTemplate client = new RestTemplate();
        for(DatabaseConfig db: dbconfigs){
            for(ServiceConfig sc:servConfigs){
                if(db.getType().equals(sc.getName()) && db.getName().equals("tib")){
                    String url = db.getUrl() + "/" + sc.getEndpoints().get("resources").getPath();
                    System.out.println(url);
                    String resp = client.getForObject(url, String.class);
                    System.out.println(resp);
                    break;
                }
            }
        }

        System.out.println("Scheduled job ran successfully...");
    }

}
