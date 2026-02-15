package net.devstudy.resume.ms.notification.messaging;

import org.apache.kafka.common.TopicPartition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

@Configuration
@ConditionalOnProperty(name = "app.notification.messaging.enabled", havingValue = "true")
public class NotificationKafkaConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> notificationKafkaListenerFactory(
            ConsumerFactory<String, String> consumerFactory,
            KafkaTemplate<String, String> kafkaTemplate,
            NotificationMessagingProperties properties) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(buildErrorHandler(kafkaTemplate, properties));
        return factory;
    }

    private DefaultErrorHandler buildErrorHandler(KafkaTemplate<String, String> kafkaTemplate,
            NotificationMessagingProperties properties) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, ex) -> new TopicPartition(record.topic() + ".dlq", record.partition()));

        NotificationMessagingProperties.Retry retry = properties.getRetry();
        int maxRetries = Math.max(0, Math.max(1, retry.getMaxAttempts()) - 1);
        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(maxRetries);
        backOff.setInitialInterval(Math.max(100L, retry.getInitialDelayMs()));
        backOff.setMaxInterval(Math.max(backOff.getInitialInterval(), retry.getMaxDelayMs()));
        backOff.setMultiplier(Math.max(1.0d, retry.getMultiplier()));

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);
        errorHandler.setCommitRecovered(true);
        return errorHandler;
    }
}
