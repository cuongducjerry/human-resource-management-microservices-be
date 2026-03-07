package com.hrm.leave.config;

import com.hrm.leave.event.LeaveApprovedEvent;
import com.hrm.leave.event.LeaveCancelledEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class LeaveKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishLeaveApproved(LeaveApprovedEvent event) {

        log.info("Sending Kafka event for leave approve: {}", event);

        kafkaTemplate.send(
                "leave-approved-topic",
                event
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishLeaveCancelled(LeaveCancelledEvent event) {

        log.info("Sending Kafka event for leave cancel: {}", event);

        kafkaTemplate.send(
                "leave-cancelled-topic",
                event
        );
    }

}
