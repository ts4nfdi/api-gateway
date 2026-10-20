package org.semantics.apigateway.collections.models;

import org.semantics.apigateway.artefacts.metadata.TerminologyStatistic;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public record TerminologyStatisticDto(
        LocalDate created,
        Map<String, Long> count
) {
    public static final String PROPERTY = "http://www.w3.org/1999/02/22-rdf-syntax-ns#Property";
    public static final String CLASS = "http://www.w3.org/2002/07/owl#Class";
    public static final String INDIVIDUAL = "http://www.w3.org/2002/07/owl#NamedIndividual";
    public static final String CONCEPT = "http://www.w3.org/2004/02/skos/core#Concept";

    public static TerminologyStatisticDto from(TerminologyStatistic statistic) {
        Map<String, Long> count = new LinkedHashMap<>();
        count.put(PROPERTY, statistic.getPropertyCount());
        count.put(CLASS, statistic.getClassCount());
        count.put(INDIVIDUAL, statistic.getNamedIndividualCount());
        count.put(CONCEPT, statistic.getConceptCount());
        return new TerminologyStatisticDto(statistic.getTimestamp().toLocalDate(), count);
    }
}