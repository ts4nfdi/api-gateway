package org.semantics.nfdi.controller;

import org.semantics.nfdi.model.Term;
import org.semantics.nfdi.service.TerminologyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TerminologyController {

    private final TerminologyService terminologyService;

    @Autowired
    public TerminologyController(TerminologyService terminologyService) {
        this.terminologyService = terminologyService;
    }


    @GetMapping("/search")
    public ResponseEntity<Term> searchForTerm(@RequestParam String query) {
        Term result = terminologyService.searchForTerm(query);
        return ResponseEntity.ok(result);
    }

}
