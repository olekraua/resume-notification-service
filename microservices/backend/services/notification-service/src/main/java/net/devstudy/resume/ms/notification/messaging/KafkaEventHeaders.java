package net.devstudy.resume.ms.notification.messaging;

public final class KafkaEventHeaders {

    public static final String EVENT_ID = "x-event-id";
    public static final String EVENT_TYPE = "x-event-type";
    public static final String SOURCE_SERVICE = "x-source-service";
    public static final String OCCURRED_AT = "x-occurred-at";

    private KafkaEventHeaders() {
    }
}
