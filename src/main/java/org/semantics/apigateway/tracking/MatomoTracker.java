package org.semantics.apigateway.tracking;

import com.google.gson.Gson;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Buffers {@link MatomoEvent}s and ships them to Matomo with the bulk tracking API.
 *
 * <p>Requests never wait for Matomo: {@link #track(MatomoEvent)} only offers to a bounded queue and returns.
 * A scheduled flush drains the queue in fifo order, which also satisfies Matomo's requirement that the entries of a
 * bulk request are ordered oldest first.
 *
 * <p>Only created when {@code matomo.enabled} is true, like the rest of the tracking.
 */
@Service
@ConditionalOnProperty(prefix = "matomo", name = "enabled", havingValue = "true")
public class MatomoTracker {

    private static final Logger logger = LoggerFactory.getLogger(MatomoTracker.class);

    /** Matomo drops a bulk request whose entries are older than this without a token_auth. */
    private static final Duration MAX_UNAUTHENTICATED_BACKDATING = Duration.ofHours(24);

    private static final Duration DROP_LOG_INTERVAL = Duration.ofMinutes(1);

    private final MatomoProperties properties;
    private final WebClient webClient;
    private final Gson gson = new Gson();

    private final BlockingQueue<MatomoEvent> queue;
    private final AtomicLong dropped = new AtomicLong();
    private final AtomicLong lastDropLogNanos = new AtomicLong();

    public MatomoTracker(MatomoProperties properties, WebClient.Builder webClientBuilder) {
        this.properties = properties;
        this.queue = new ArrayBlockingQueue<>(Math.max(1, properties.getQueueCapacity()));
        this.webClient = webClientBuilder.build();
    }

    /**
     * Queues an event. Returns false and increments the dropped counter when the queue is full, rather than blocking
     * the request thread: losing analytics is always preferable to slowing down the API.
     */
    public boolean track(MatomoEvent event) {
        if (event == null) {
            return false;
        }
        if (queue.offer(event)) {
            return true;
        }
        long total = dropped.incrementAndGet();
        logDropped(total);
        return false;
    }

    @Scheduled(fixedDelayString = "${matomo.flush-interval-ms:10000}")
    public void flush() {
        List<MatomoEvent> batch = new ArrayList<>(properties.getBatchSize());
        queue.drainTo(batch, properties.getBatchSize());
        if (batch.isEmpty()) {
            return;
        }
        send(batch);
    }

    /**
     * Drains whatever is still buffered on shutdown so a restart does not silently lose the queue.
     * Unlike the scheduled flush this one waits for the response, because the reactive machinery is torn down as
     * soon as this method returns and an unfinished request would simply be dropped.
     */
    @PreDestroy
    public void flushOnShutdown() {
        List<MatomoEvent> remaining = new ArrayList<>();
        queue.drainTo(remaining);
        for (int i = 0; i < remaining.size(); i += properties.getBatchSize()) {
            send(remaining.subList(i, Math.min(i + properties.getBatchSize(), remaining.size())), true);
        }
    }

    private void send(List<MatomoEvent> batch) {
        send(batch, false);
    }

    private void send(List<MatomoEvent> batch, boolean blocking) {
        List<String> requests = new ArrayList<>(batch.size());
        for (MatomoEvent event : batch) {
            requests.add(buildQuery(event));
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("requests", requests);
        if (needsTokenAuth()) {
            body.put("token_auth", properties.getTokenAuth());
        }
        String payload = gson.toJson(body);

        if (properties.getUrl() == null || properties.getUrl().isBlank()) {
            logger.warn("matomo.url is not configured, discarding {} event(s)", batch.size());
            return;
        }

        // Set org.semantics.apigateway.tracking=TRACE to see the exact payload while verifying an integration.
        logger.trace("Matomo bulk payload: {}", payload);

        try {
            Mono<ResponseEntity<Void>> call = webClient.post()
                    .uri(properties.getUrl())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .toBodilessEntity()
                    .timeout(Duration.ofMillis(properties.getTimeoutMs()))
                    .doOnNext(response -> logger.debug("Sent {} event(s) to Matomo, response {}",
                            batch.size(), response.getStatusCode()))
                    .doOnError(error -> logger.warn("Failed to send {} event(s) to Matomo: {}",
                            batch.size(), error.toString()))
                    .onErrorComplete();

            if (blocking) {
                call.block(Duration.ofMillis(properties.getTimeoutMs()));
            } else {
                call.subscribe();
            }
        } catch (Exception e) {
            // Tracking must never propagate a failure into the application.
            logger.warn("Failed to submit {} event(s) to Matomo: {}", batch.size(), e.toString());
        }
    }

    /**
     * Builds one entry of the bulk "requests" array, i.e. the query string that would be sent if the event were
     * tracked on its own.
     */
    String buildQuery(MatomoEvent event) {
        StringBuilder query = new StringBuilder("?idsite=")
                .append(properties.getSiteId())
                .append("&rec=1")
                // Ask for a 204 instead of the tracking gif.
                .append("&send_image=0")
                // Report the real time of the request, not the time the batch happens to be flushed.
                .append("&cdt=").append(event.timestamp().getEpochSecond());

        append(query, "url", event.url());
        append(query, "action_name", event.actionName());
        append(query, "_id", event.visitorId());
        append(query, "ua", event.userAgent());
        append(query, "lang", event.language());
        append(query, "urlref", event.referrer());

        if (properties.isTrackUserId()) {
            append(query, "uid", event.userId());
        }
        if (properties.isTrackClientIp()) {
            append(query, "cip", event.clientIp());
        }
        if (properties.isTrackSiteSearch() && isNotBlank(event.searchQuery())) {
            append(query, "search", event.searchQuery());
        }
        if (event.durationMs() > 0) {
            // Matomo expects the generation time in milliseconds.
            query.append("&pf_srv=").append(event.durationMs());
        }
        if (event.dimensions() != null) {
            event.dimensions().forEach((index, value) -> {
                if (index != null && index > 0) {
                    append(query, "dimension" + index, value);
                }
            });
        }
        return query.toString();
    }

    /**
     * The token is only needed for parameters Matomo gates behind authentication. {@code cdt} is free as long as the
     * event is younger than 24 hours, which our flush interval guarantees, so only the ip override forces a token.
     */
    private boolean needsTokenAuth() {
        return properties.isTrackClientIp() && isNotBlank(properties.getTokenAuth());
    }

    private void append(StringBuilder query, String key, String value) {
        if (!isNotBlank(value)) {
            return;
        }
        query.append('&').append(key).append('=').append(URLEncoder.encode(value, StandardCharsets.UTF_8));
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    private void logDropped(long total) {
        long now = System.nanoTime();
        long last = lastDropLogNanos.get();
        if (now - last < DROP_LOG_INTERVAL.toNanos() && last != 0) {
            return;
        }
        if (lastDropLogNanos.compareAndSet(last, now)) {
            logger.warn("Matomo event queue is full, dropped {} event(s) so far. "
                    + "Consider raising matomo.queue-capacity or lowering matomo.flush-interval-ms.", total);
        }
    }

    /** Number of events dropped because the queue was full. */
    public long getDroppedCount() {
        return dropped.get();
    }

    /** Current queue depth, exposed for tests and troubleshooting. */
    public int getQueueSize() {
        return queue.size();
    }

    /** Longest backdating Matomo accepts without a token, kept for documentation of the cdt choice. */
    static Duration maxUnauthenticatedBackdating() {
        return MAX_UNAUTHENTICATED_BACKDATING;
    }
}
