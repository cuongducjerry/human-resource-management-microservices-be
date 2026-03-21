package com.hrm.organization.config;

import com.hrm.organization.dto.request.OrganizationDashboardDTO;
import com.hrm.organization.dto.request.PositionDashboardDTO;
import com.hrm.organization.event.DashboardEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class DashboardPositionKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT,
            condition = "#event.type.startsWith('POSITION')"
    )
    public void handlePositionEvent(DashboardEvent event) {

        log.info("Sending Kafka event for position: {}", event);

        kafkaTemplate.send("dashboard-topic", event);
    }
}