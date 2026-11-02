The **TS4NFDI API Gateway** provides unified access to terminology services in the NFDI ecosystem and beyond.

Instead of integrating with each service separately, you can query services such as OLS instances, OntoPortal instances (for example AgroPortal and BiodivPortal), Skosmos instances, GND, and others through **one consistent API**.

The gateway:

- forwards requests to connected terminology services in parallel;
- translates service-specific responses into a common format;
- returns a single, combined result in supported formats.

Responses are returned as JSON and include JSON-LD annotations such as `@context` and `@type`.

## What you can do

| Use case | Description | Relevant sections |
|---|---|---|
| **Search terms** | Search for classes, concepts, and other terms across all connected terminology services at once. | **MOD - Search** |
| **Explore artefacts** | List and inspect ontologies, thesauri, and vocabularies, including title, description, version, and hosting service. | **MOD - Artefacts** |
| **Browse artefact content** | Explore classes/concepts, properties, individuals, concept schemes, SKOS collections, and class/concept hierarchies. | **MOD - Artefact Data** |
| **Use existing OLS clients** | Use OLS-compatible endpoints that accept OLS-style requests and return OLS-format responses, while accessing data from all connected services. | **OLS4 v2**, **OLS4** |
| **Create collections** | Create named, shareable sets of terminologies and restrict other requests to those terminologies. | **Collections**, **Users**, **Authentication** |
| **Check service availability** | See which terminology services are connected and whether they are reachable. | **Configuration**, **Status** |


## Quick start

### Search across all services

Try a simple search to retrieve combined results from all connected services:

```http
GET /search?query=plant
```

### Find an ontology or vocabulary

List the available artefacts:

```http
GET /artefacts
```

Then use an artefact’s encoded ID or `short_form` as `{id}` in the artefact endpoints.

Examples:

```http
GET /artefacts/https%3A%2F%2Flobid.org%2Fgnd/...
GET /artefacts/ncit/...
```

### Check connected services

See which terminology services are configured:

```http
GET /config/databases
```

### View example requests

Explore ready-to-use example requests:

```http
GET /status/examples
```

## How it works

The gateway is configured through JSON configuration files.

Each file describes:

- a connected terminology service;
- how that service’s API maps to the gateway’s unified endpoints.

This design allows new terminology services to be added, or existing services to be reconfigured, **without changing the public API** used by client applications.


## API foundation

The gateway API is based on the [MOD-API](https://github.com/FAIR-IMPACT/MOD-API) and uses its terminology.

MOD-API is an API specification for metadata describing semantic artefacts, including ontologies, terminologies, taxonomies, thesauri, vocabularies, metadata schemas, and semantic standards. It is designed to support interoperable access to semantic artefact catalogues. [1]

The following sections explain the terminology used throughout this documentation.