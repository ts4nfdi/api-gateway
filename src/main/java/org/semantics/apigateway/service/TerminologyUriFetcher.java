package org.semantics.apigateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.semantics.apigateway.model.terminology.TerminologyIdUriMap;
import org.semantics.apigateway.repository.TerminologyIdUriMapRepository;

import java.util.Optional;

@Service
public class TerminologyUriFetcher {

    @Autowired
    private TerminologyIdUriMapRepository idUriMapRepository;

    @Scheduled(fixedRate = 10000)
    public void run(){
        TerminologyIdUriMap tIdUriMap = new TerminologyIdUriMap();
        tIdUriMap.setTerminologyId("vibso");
        tIdUriMap.setUri("http://purl.obolibrary.org/obo/vibso.owl");
        Optional<TerminologyIdUriMap> record = this.idUriMapRepository.findByTerminologyId(tIdUriMap.getTerminologyId());
        if(!record.isPresent()){
            this.idUriMapRepository.save(tIdUriMap);
        }
        System.out.println("Scheduled job ran successfully...");
    }

}
