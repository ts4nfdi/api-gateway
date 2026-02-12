package org.semantics.apigateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.semantics.apigateway.model.terminology.TerminologyIdUriMap;
import org.semantics.apigateway.repository.TerminologyIdUriMapRepository;
import org.semantics.apigateway.service.configuration.ConfigurationLoader;
import org.semantics.apigateway.config.DatabaseConfig;
import org.semantics.apigateway.config.ServiceConfig;

import java.util.Optional;
import java.util.List;

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
        tIdUriMap.setUri("http://purl.obolibrary.org/obo/vibso.owl");
        Optional<TerminologyIdUriMap> record = this.idUriMapRepository.findByTerminologyId(tIdUriMap.getTerminologyId());
        if(!record.isPresent()){
            this.idUriMapRepository.save(tIdUriMap);
        }
        List<DatabaseConfig> dbconfigs = configLoader.getDatabaseConfigs();
        List<ServiceConfig> servConfigs = configLoader.getServiceConfigs();
        for(DatabaseConfig db: dbconfigs){
            System.out.println(db.getName());
            System.out.println(db.getUrl());
        }
        for(ServiceConfig sc:servConfigs){
            System.out.println(sc.getName());
            System.out.println(sc.getEndpoints().get("resources").getPath());
        }
        System.out.println("Scheduled job ran successfully...");
    }

}
