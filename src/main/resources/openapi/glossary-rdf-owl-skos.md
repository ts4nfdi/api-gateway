Terminologies are usually published in one of the following W3C standards:

- **[RDF](https://www.w3.org/RDF/) (Resource Description Framework):** a data model that represents information as *subject – predicate – object* statements, e.g. "Paris – is capital of – France". OWL and SKOS both build on RDF.
- **[OWL](https://www.w3.org/TR/owl-ref/) (Web Ontology Language):** a formally defined language for *ontologies*, i.e. detailed, logical models of a subject area with classes, properties, individuals, data values, and logical rules (axioms). Think of a smart, computer-readable encyclopedia structure.
- **[SKOS](https://www.w3.org/TR/skos-reference/) (Simple Knowledge Organization System):** a simpler model for controlled vocabularies, taxonomies, thesauri, classification schemes, and tag/keyword systems. It organizes concepts and how they relate (broader, narrower, related terms).

The gateway returns content of both OWL ontologies and SKOS vocabularies. Some words mean slightly different things in the two worlds:

| Word | In OWL | In SKOS |
|------|--------|---------|
| **Term** | Any entity used to describe a domain of interest. The main OWL entities are classes, properties, and individuals. Each has a unique identifier (IRI). | A text label of a concept, e.g. its preferred label, alternative label, or hidden label. |
| **Class** | A category and, formally, a set of individuals that have something in common (`owl:Class`), e.g. "Car". | SKOS itself only uses a few fixed classes: `skos:Concept`, `skos:ConceptScheme` (a whole vocabulary), and `skos:Collection` (a group of concepts). |
| **Concept** | – | One entry of a vocabulary (`skos:Concept`), e.g. "Renewable energy". Concepts play a role similar to OWL classes. |
| **Property** | A relation or attribute. Object properties relate individuals to individuals, datatype properties relate individuals to data values (e.g. "has colour"). | A relationship between concepts, e.g. "broader", "narrower", or "in scheme". |
| **Individual** | A concrete member (instance) of a class, e.g. "my car" is an individual of the class "Car". | – (each concept is already a concrete entry) |

In this API, *entity* is used as umbrella word for terms, classes, concepts, properties, and individuals.
