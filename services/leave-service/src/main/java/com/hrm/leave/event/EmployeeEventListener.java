package com.hrm.leave.event;

import com.hrm.leave.service.LeaveService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeEventListener {

    private final LeaveService leaveService;

    @PostConstruct
    public void init() {
        System.out.println("Kafka listener bean loaded");
    }

    @KafkaListener(topics = "employee-created-topic", groupId = "leave-group")
    public void handle(String employeeId) {

        System.out.println("Received: " + employeeId);

        leaveService.initLeaveBalance(UUID.fromString(employeeId));
    }
}
