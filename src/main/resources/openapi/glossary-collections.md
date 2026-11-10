- **Terminology collection:** a set of terminologies, created for a specific purpose or context in which this set is relevant, e.g. "all terminologies relevant for plant research". A terminology collection can be shared with the community and can contain ontologies, thesauri, etc., without any rating. It is a grouping within the vast space of existing terminologies. Terminology collections are managed in the **Collections** group; pass the `id` of a terminology collection as `collectionId` to restrict other requests to its terminologies.
- **Entity set:** a flat, optionally ordered set of selected IRIs, created for a specific purpose or context. It contains no semantic relations among its entities and can consist of entities from several terminologies and providers. There are no dedicated entity set endpoints yet.
- **Provider (also, but deprecated: source, database):** an external terminology service or vocabulary provider that the gateway connects to, e.g. an OLS, OntoPortal, or Skosmos instance. Each provider is an independent, third-party system that maintains its own terminologies and exposes them through its own API. In parameters and responses, a provider is called `database` (e.g. the `database` parameter, `GET /config/databases`) or `source` (e.g. the `source_name` field). The connected providers are listed in the **Reference** section below.

The word **collection** has three different meanings in this API:

| Kind | Meaning | Where it appears |
|------|---------|------------------|
| **Terminology collection** (TS4NFDI) | A set of terminologies, see above. | The **Collections** group, the `collectionId` parameter. |
| **SKOS collection** (`skos:Collection`) | A labeled group of SKOS concepts within one vocabulary, used for organization or display. | `GET /artefacts/{id}/resources/collections` |
| **Response collection** | The list of result items in a response. | The `collection` field of the response body. |
