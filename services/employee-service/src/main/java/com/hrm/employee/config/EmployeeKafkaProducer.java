package com.hrm.employee.config;

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
public class EmployeeKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEmployeeCreated(EmployeeCreatedEvent event) {

        log.info("Sending Kafka event for employee: {}", event);

        kafkaTemplate.send(
                "employee-created-topic",
                event
        );
    }
}