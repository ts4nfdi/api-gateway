package org.semantics.nfdi.service;

import org.semantics.nfdi.model.Term;
import org.springframework.stereotype.Service;

@Service
public class TerminologyService {

    public Term searchForTerm(String query) {
        Term term = new Term();
        term.setId("mockId");
        term.setName(query);
        term.setDescription("This is a mock description for the search term.");
        return term;
    }
}
