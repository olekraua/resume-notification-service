package net.devstudy.resume.ms.notification.messaging;

import java.util.Map;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

import net.devstudy.resume.notification.api.messaging.NotificationMessaging;

@Configuration
@ConditionalOnProperty(name = "app.notification.messaging.enabled", havingValue = "true")
public class NotificationAmqpConfig {

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NotificationMessaging.EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange notificationRetryExchange() {
        return new TopicExchange(NotificationMessaging.RETRY_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange notificationDlxExchange() {
        return new TopicExchange(NotificationMessaging.DLX_EXCHANGE, true, false);
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NotificationMessaging.QUEUE)
                .withArguments(Map.of(
                        "x-dead-letter-exchange", NotificationMessaging.RETRY_EXCHANGE,
                        "x-dead-letter-routing-key", NotificationMessaging.RETRY_ROUTING_KEY))
                .build();
    }

    @Bean
    public Binding notificationBinding(Queue notificationQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(notificationQueue)
                .to(notificationExchange)
                .with(NotificationMessaging.ROUTING_KEY_RESTORE);
    }

    @Bean
    public Queue notificationRetryQueue(
            @Value("${app.notification.messaging.retry-delay-ms:30000}") long retryDelayMs) {
        long safeDelay = Math.max(1000L, retryDelayMs);
        return QueueBuilder.durable(NotificationMessaging.RETRY_QUEUE)
                .withArguments(Map.of(
                        "x-message-ttl", safeDelay,
                        "x-dead-letter-exchange", NotificationMessaging.EXCHANGE,
                        "x-dead-letter-routing-key", NotificationMessaging.ROUTING_KEY_RESTORE))
                .build();
    }

    @Bean
    public Binding notificationRetryBinding(Queue notificationRetryQueue, TopicExchange notificationRetryExchange) {
        return BindingBuilder.bind(notificationRetryQueue)
                .to(notificationRetryExchange)
                .with(NotificationMessaging.RETRY_ROUTING_KEY);
    }

    @Bean
    public Queue notificationDlq() {
        return QueueBuilder.durable(NotificationMessaging.DLQ).build();
    }

    @Bean
    public Binding notificationDlqBinding(Queue notificationDlq, TopicExchange notificationDlxExchange) {
        return BindingBuilder.bind(notificationDlq)
                .to(notificationDlxExchange)
                .with(NotificationMessaging.DLQ_ROUTING_KEY);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory notificationListenerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}
