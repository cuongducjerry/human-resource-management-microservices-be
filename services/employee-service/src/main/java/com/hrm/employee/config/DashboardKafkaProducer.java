package com.hrm.employee.config;

import com.hrm.employee.event.DashboardEvent;
import com.hrm.employee.event.EmployeeCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class DashboardKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDashboardCreated(DashboardEvent event) {

        log.info("Sending Kafka event for dashboard employee: {}", event);

        kafkaTemplate.send(
                "dashboard-topic",
                event
        );
    }

}
