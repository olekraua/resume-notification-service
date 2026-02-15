package net.devstudy.resume.ms.notification.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.kafka.support.KafkaHeaders;

import net.devstudy.resume.notification.api.event.RestoreAccessMailRequestedEvent;
import net.devstudy.resume.notification.internal.service.RestoreAccessMailService;

@Component
@ConditionalOnProperty(name = "app.notification.messaging.enabled", havingValue = "true")
public class NotificationConsumer {

    private final ObjectMapper objectMapper;
    private final RestoreAccessMailService mailService;
    private final NotificationMessagingProperties properties;
    private final ProcessedEventStore processedEventStore;

    public NotificationConsumer(ObjectMapper objectMapper,
            RestoreAccessMailService mailService,
            NotificationMessagingProperties properties,
            ProcessedEventStore processedEventStore) {
        this.objectMapper = objectMapper;
        this.mailService = mailService;
        this.properties = properties;
        this.processedEventStore = processedEventStore;
    }

    @KafkaListener(
            topics = "${app.notification.messaging.kafka.topic-restore-access-mail}",
            groupId = "${app.notification.messaging.consumer-group:resume-notification-mail}",
            containerFactory = "notificationKafkaListenerFactory")
    public void onMessage(String payload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(name = KafkaEventHeaders.EVENT_ID, required = false) String eventId)
            throws Exception {
        if (payload == null || payload.isBlank()) {
            return;
        }
        if (!properties.getKafka().getTopicRestoreAccessMail().equals(topic)) {
            throw new IllegalArgumentException("Unsupported topic: " + topic);
        }
        String dedupKey = resolveDedupKey(eventId, topic, payload);
        if (processedEventStore.isAlreadyProcessed(dedupKey)) {
            return;
        }
        RestoreAccessMailRequestedEvent event = objectMapper.readValue(payload, RestoreAccessMailRequestedEvent.class);
        if (event == null) {
            return;
        }
        mailService.sendRestoreLink(event.email(), event.firstName(), event.link());
        processedEventStore.markProcessed(dedupKey);
    }

    private String resolveDedupKey(String eventId, String topic, String payload) {
        if (eventId != null && !eventId.isBlank()) {
            return eventId;
        }
        return topic + "|" + Integer.toHexString(payload.hashCode());
    }
}
