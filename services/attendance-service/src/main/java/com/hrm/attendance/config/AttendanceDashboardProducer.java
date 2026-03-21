package com.hrm.attendance.config;

import com.hrm.attendance.event.DashboardEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class AttendanceDashboardProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishAttendanceEvent(DashboardEvent event) {
        log.info("Sending Kafka event for attendance dashboard: {}", event);
        kafkaTemplate.send("dashboard-topic", event);
    }
}
