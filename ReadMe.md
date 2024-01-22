# NFDI Federated Search Service

## Overview

The NFDI Federated Search Service is an advanced, dynamic solution designed to perform federated searches across multiple databases containing ontologies. It is particularly tailored for environments where integration and aggregation of diverse data sources are essential. The service offers faceted search capabilities, enabling users to refine search results based on specific criteria, and supports responses in both JSON and JSON-LD formats.

A standout feature of this service is its dynamic nature, governed by a YAML configuration file. This design choice allows for easy extension and customization of the service to include new databases or modify existing configurations.

## Features

- **Federated Search Across Multiple Databases:** Seamlessly query multiple databases simultaneously and aggregate results into a unified format.
- **Faceted Search Capabilities:** Filter and refine search results based on specific criteria, enhancing the search experience and relevance of results.
- **Dynamic Configuration:** Utilize a YAML file to configure database connections and response mappings, enabling easy addition or modification of data sources.
- **Response Format Flexibility:** Choose between standard JSON and JSON-LD formats for search results, catering to different use cases and requirements.
- **Schema Transformation:** Convert search responses into specific database schemas, facilitating integration with various database-driven systems.

## Installation

To set up the NFDI Federated Search Service, follow these steps:

1. Clone the repository to your local machine:
   git clone https://gitlab.fokus.fraunhofer.de/dan48526/nfdi
2. Navigate to the project directory:
   `cd nfdi-federated-search-service`

3. Build and compile the service:
   `mvn clean install`
4. Run the service:
   `java -jar target/nfdi-federated-search-service.jar`


The service will be accessible at `http://localhost:8080` by default.

## Extensibility and Customization

The service's dynamic configuration approach allows for straightforward extensibility. Adding a new database or modifying an existing one involves updating the YAML configuration file with the relevant details and mappings. This flexibility ensures that the service can adapt to evolving data sources and requirements without the need for significant code changes.

### Customizing Database Schema Mapping

The mapping from the JSON response to a database schema is hardcoded in the `DyndatabaseTransform.java` class. You can customize this mapping by following these steps:

1. Locate the `DyndatabaseTransform.java` class in your project directory.

2. Open the class and review the existing mapping logic. You'll find code sections responsible for mapping JSON data to the database schema.

3. Modify the mapping logic as needed to align with your specific database schema requirements.

4. Save your changes.

5. Rebuild and compile the service using the following commands:
   `mvn clean install`

6. Restart the service:
   `java -jar target/nfdi-federated-search-service.jar`


Your custom database schema mapping will now be applied to the search responses.

Remember to test your changes thoroughly to ensure that the mapping accurately reflects your database schema and that the service functions as expected.








