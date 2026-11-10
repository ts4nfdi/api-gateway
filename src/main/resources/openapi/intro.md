The **TS4NFDI API Gateway** provides unified access to terminology services in the NFDI ecosystem and beyond.

Instead of integrating with each service separately, you can query services such as OLS instances, OntoPortal instances (for example AgroPortal and BiodivPortal), Skosmos instances, GND, and others through **one consistent API**.

The gateway:

- forwards requests to connected terminology services in parallel;
- translates service-specific responses into a common format;
- returns a single, combined result in supported formats.

Responses are returned as JSON and include JSON-LD annotations such as `@context` and `@type`.