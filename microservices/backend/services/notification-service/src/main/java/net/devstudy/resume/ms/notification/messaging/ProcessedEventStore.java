package net.devstudy.resume.ms.notification.messaging;

import java.time.Duration;
import java.util.Objects;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

@Component
@ConditionalOnProperty(name = "app.notification.messaging.enabled", havingValue = "true")
public class ProcessedEventStore {

    private final boolean enabled;
    private final Cache<String, Boolean> processedEvents;

    public ProcessedEventStore(NotificationMessagingProperties properties) {
        NotificationMessagingProperties.Idempotency idempotency = properties.getIdempotency();
        this.enabled = idempotency.isEnabled();
        if (!enabled) {
            this.processedEvents = null;
            return;
        }
        long maxSize = Math.max(1000L, idempotency.getMaxSize());
        Duration ttl = Objects.requireNonNullElse(idempotency.getTtl(), Duration.ofHours(24));
        this.processedEvents = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(ttl)
                .build();
    }

    public boolean isAlreadyProcessed(String eventId) {
        if (!enabled) {
            return false;
        }
        if (eventId == null || eventId.isBlank()) {
            return false;
        }
        return processedEvents.getIfPresent(eventId) != null;
    }

    public void markProcessed(String eventId) {
        if (!enabled) {
            return;
        }
        if (eventId == null || eventId.isBlank()) {
            return;
        }
        processedEvents.put(eventId, Boolean.TRUE);
    }
}
