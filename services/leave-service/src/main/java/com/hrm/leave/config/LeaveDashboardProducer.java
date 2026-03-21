package com.hrm.leave.config;

import com.hrm.leave.event.DashboardEvent;
import com.hrm.leave.event.LeaveApprovedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class LeaveDashboardProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishLeaveDashboardApproved(DashboardEvent event) {

        log.info("Sending Kafka event for leave approve: {}", event);

        kafkaTemplate.send(
                "dashboard-topic",
                event
        );
    }

}
