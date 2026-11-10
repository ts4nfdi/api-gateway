Every request that reads terminology data is forwarded to all queried providers in parallel. `timeout` limits how long the gateway waits for each single provider:

- **A provider answers within the timeout:** its results are included in the aggregated response.
- **A provider does not answer within the timeout:** that provider is skipped and contributes no results. The results of all providers that answered in time are returned as usual (partial result). The response does not yet indicate which providers were skipped.
- **A provider answers with an error** (e.g. HTTP 404 or 500, or the connection fails): that provider is skipped as well, and the results of all other providers are returned as usual.

This matters most for the search endpoints, which query multiple providers at once. Because provider response times vary, **the same request can return results from different providers at different times.** A missing or empty result therefore does not necessarily mean that no matching terms exist.

Further details:

- The timeout is limited by a maximum that is configured by the gateway administrator and might therefore vary between installations of the API Gateway (environment variable `REQUEST_TIMEOUT_MS_MAXVALUE`, default `60000` = 60 seconds). If `timeout` is omitted, negative, or larger than this maximum, the maximum is used instead.
- Since the providers are queried in parallel, waiting for them takes at most about `timeout`. Aggregating and transforming the results (e.g. for `targetDbSchema`) happens afterwards, so the total response time can be slightly longer than `timeout`.
- A smaller `timeout` returns faster but makes it more likely that slow providers are missing from the result. To get complete results quickly, use the `database` parameter to query only the providers you need, and leave out providers that are known to be slow.

Example: `GET /search?query=plant&database=ols2,ontoportal&timeout=10000`
