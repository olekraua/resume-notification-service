package net.devstudy.resume.ms.notification.messaging;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import net.devstudy.resume.notification.api.event.RestoreAccessMailRequestedEvent;
import net.devstudy.resume.notification.api.messaging.NotificationMessaging;
import net.devstudy.resume.notification.internal.service.RestoreAccessMailService;

@Component
@ConditionalOnProperty(name = "app.notification.messaging.enabled", havingValue = "true")
public class NotificationConsumer {

    private final ObjectMapper objectMapper;
    private final RestoreAccessMailService mailService;
    private final RabbitTemplate rabbitTemplate;
    private final int maxAttempts;

    public NotificationConsumer(ObjectMapper objectMapper,
            RestoreAccessMailService mailService,
            RabbitTemplate rabbitTemplate,
            @Value("${app.notification.messaging.max-attempts:5}") int maxAttempts) {
        this.objectMapper = objectMapper;
        this.mailService = mailService;
        this.rabbitTemplate = rabbitTemplate;
        this.maxAttempts = Math.max(1, maxAttempts);
    }

    @RabbitListener(queues = NotificationMessaging.QUEUE,
            containerFactory = "notificationListenerFactory")
    public void onMessage(String payload,
            Message message,
            @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String routingKey)
            throws Exception {
        if (!NotificationMessaging.ROUTING_KEY_RESTORE.equals(routingKey)) {
            return;
        }
        if (payload == null || payload.isBlank()) {
            return;
        }
        if (isOverAttempts(message)) {
            rabbitTemplate.convertAndSend(NotificationMessaging.DLX_EXCHANGE,
                    NotificationMessaging.DLQ_ROUTING_KEY, payload);
            return;
        }
        RestoreAccessMailRequestedEvent event = objectMapper.readValue(payload,
                RestoreAccessMailRequestedEvent.class);
        if (event == null) {
            return;
        }
        mailService.sendRestoreLink(event.email(), event.firstName(), event.link());
    }

    private boolean isOverAttempts(Message message) {
        if (message == null || message.getMessageProperties() == null) {
            return false;
        }
        Object header = message.getMessageProperties().getHeaders().get("x-death");
        if (!(header instanceof List<?> list) || list.isEmpty()) {
            return false;
        }
        long deadCount = 0L;
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> map)) {
                continue;
            }
            Object queue = map.get("queue");
            if (!NotificationMessaging.QUEUE.equals(queue)) {
                continue;
            }
            Object count = map.get("count");
            if (count instanceof Long value) {
                deadCount += value;
            } else if (count instanceof Integer value) {
                deadCount += value.longValue();
            }
        }
        long attempts = deadCount + 1;
        return attempts > maxAttempts;
    }
}
