package com.hrm.payroll.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrm.payroll.event.AttendanceRecordedEvent;
import com.hrm.payroll.service.AttendanceSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AttendanceEventListener {

    private final AttendanceSummaryService attendanceSummaryService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "attendance-recorded-topic", groupId = "payroll-group")
    public void handle(String message) throws Exception {

        System.out.println("RAW JSON: " + message);

        AttendanceRecordedEvent event =
                objectMapper.readValue(message, AttendanceRecordedEvent.class);

        attendanceSummaryService.initAttendanceSummary(event);
    }

}
