package com.hrm.payroll.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrm.payroll.event.LeaveApprovedEvent;
import com.hrm.payroll.event.LeaveCancelledEvent;
import com.hrm.payroll.service.LeaveSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LeaveEventListener {

    private final LeaveSummaryService leaveSummaryService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "leave-approved-topic", groupId = "payroll-group")
    public void handleLeaveApproved(String message) throws Exception {

        System.out.println("RAW LEAVE APPROVED JSON: " + message);

        LeaveApprovedEvent event = objectMapper.readValue(message, LeaveApprovedEvent.class);

        leaveSummaryService.applyApprovedLeave(event);
    }

    @KafkaListener(topics = "leave-cancelled-topic", groupId = "payroll-group")
    public void handleLeaveCancelled(String message) throws Exception {

        System.out.println("RAW LEAVE CANCELLED JSON: " + message);

        LeaveCancelledEvent event = objectMapper.readValue(message, LeaveCancelledEvent.class);

        leaveSummaryService.rollbackCancelledLeave(event);
    }

}
