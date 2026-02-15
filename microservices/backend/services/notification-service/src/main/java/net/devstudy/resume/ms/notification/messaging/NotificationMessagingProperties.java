package net.devstudy.resume.ms.notification.messaging;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.notification.messaging")
public class NotificationMessagingProperties {

    private boolean enabled;
    private String consumerGroup = "resume-notification-mail";
    private final Kafka kafka = new Kafka();
    private final Retry retry = new Retry();
    private final Idempotency idempotency = new Idempotency();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public Kafka getKafka() {
        return kafka;
    }

    public Retry getRetry() {
        return retry;
    }

    public Idempotency getIdempotency() {
        return idempotency;
    }

    public static class Kafka {
        private String topicRestoreAccessMail = "resume.auth.restore-access-mail";

        public String getTopicRestoreAccessMail() {
            return topicRestoreAccessMail;
        }

        public void setTopicRestoreAccessMail(String topicRestoreAccessMail) {
            this.topicRestoreAccessMail = topicRestoreAccessMail;
        }
    }

    public static class Retry {
        private int maxAttempts = 5;
        private long initialDelayMs = 1000L;
        private long maxDelayMs = 30000L;
        private double multiplier = 2.0d;

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public long getInitialDelayMs() {
            return initialDelayMs;
        }

        public void setInitialDelayMs(long initialDelayMs) {
            this.initialDelayMs = initialDelayMs;
        }

        public long getMaxDelayMs() {
            return maxDelayMs;
        }

        public void setMaxDelayMs(long maxDelayMs) {
            this.maxDelayMs = maxDelayMs;
        }

        public double getMultiplier() {
            return multiplier;
        }

        public void setMultiplier(double multiplier) {
            this.multiplier = multiplier;
        }
    }

    public static class Idempotency {
        private long maxSize = 200000L;
        private Duration ttl = Duration.ofHours(24);

        public long getMaxSize() {
            return maxSize;
        }

        public void setMaxSize(long maxSize) {
            this.maxSize = maxSize;
        }

        public Duration getTtl() {
            return ttl;
        }

        public void setTtl(Duration ttl) {
            this.ttl = ttl;
        }
    }
}
