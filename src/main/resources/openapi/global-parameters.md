The following query parameters are accepted by all endpoints, except the Authentication, Collections and User endpoints:

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `timeout` | integer (milliseconds) | no | the configured maximum (`60000` = 60 seconds, unless configured otherwise) | Maximum time span the gateway waits for each queried provider to respond. Providers that are too slow are skipped. Cannot exceed the configured maximum. See *The timeout parameter* below. |
| `database` | string (comma-separated) | no | all providers | Providers to query, matched against the configured provider names or types (e.g. `ols2`,`ontoportal`, `skosmos`, `tib`, `zbmed`). Unknown values result in an error listing the possible values. |
| `targetDbSchema` | enum: `ontoportal`, `ols2`, `skosmos`, `mod` | no | gateway format | Transforms the response into the schema of the given provider type. |
| `collectionId` | string (UUID) | no | no filter | Restricts the response to artefacts contained in the given terminology collection. The terminology collection must be accessible to the current user. |
| `showResponseConfiguration` | boolean | no | `false` | Additionally returns the original, untransformed provider responses and the request configuration used to query them. |
| `displayEmptyValues` | boolean | no | `true` | If `false`, attributes without a value are removed from the result items. |
| `display` | string (comma-separated) | no | all attributes | Attributes to include in the result items; all other attributes are removed. |
| `omitArtefactsWithoutIri` | boolean | no | `false` | If `true`, only artefacts with a proper IRI as identifier are returned. Only affects artefact (terminology) queries. |
| `disableCache` | boolean | no | `false` | Disables caching of provider responses. Not implemented yet, currently has no effect. |

Example: `GET /artefacts?database=ols2,ontoportal&targetDbSchema=ols&display=iri,label&timeout=30000`
