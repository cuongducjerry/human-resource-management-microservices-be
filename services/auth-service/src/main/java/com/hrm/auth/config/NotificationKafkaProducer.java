package com.hrm.auth.config;

import com.hrm.auth.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendNotification(NotificationEvent event) {

        log.info("Sending Kafka event for notification reset password: {}", event);

        String key = event.getEmployeeId() != null
                ? event.getEmployeeId()
                : event.getEventId();

        kafkaTemplate.send(
                "notifications",
                key,
                event
        );
    }

}
