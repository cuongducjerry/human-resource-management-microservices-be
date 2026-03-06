package com.hrm.leave.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrm.leave.event.EmployeeCreatedEvent;
import com.hrm.leave.service.LeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmployeeEventListener {

    private final LeaveService leaveService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "employee-created-topic", groupId = "leave-group")
    public void handle(String message) throws Exception {

        System.out.println("RAW JSON: " + message);

        EmployeeCreatedEvent event =
                objectMapper.readValue(message, EmployeeCreatedEvent.class);

        leaveService.initLeaveBalance(event);
    }
}
