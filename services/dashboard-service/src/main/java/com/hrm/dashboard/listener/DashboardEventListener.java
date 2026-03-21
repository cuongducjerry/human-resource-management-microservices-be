package com.hrm.dashboard.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrm.dashboard.dto.request.AttendanceDashboardDTO;
import com.hrm.dashboard.dto.request.EmployeeDashboardDTO;
import com.hrm.dashboard.dto.request.LeaveDashboardDTO;
import com.hrm.dashboard.entity.LeaveEmployeeStats;
import com.hrm.dashboard.event.DashboardEvent;
import com.hrm.dashboard.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardEventListener {

    private final ObjectMapper objectMapper;
    private final EmployeeStatsService employeeStatsService;
    private final OrganizationStatsService organizationStatsService;
    private final PositionStatsService positionStatsService;
    private final LeaveStatsService leaveStatsService;
    private final AttendanceStatsService attendanceStatsService;

    @KafkaListener(topics = "dashboard-topic", groupId = "dashboard-group")
    public void handle(String message) throws Exception {

        DashboardEvent event =
                objectMapper.readValue(message, DashboardEvent.class);

        switch (event.getType()) {

            case "EMPLOYEE_CREATED":
                EmployeeDashboardDTO data = objectMapper.convertValue(event.getData(),EmployeeDashboardDTO.class);
                employeeStatsService.handleEmployeeCreated(data);
                break;

            case "ORGANIZATION_CREATED":
                organizationStatsService.handleOrganizationCreated();
                break;

            case "ORGANIZATION_DELETED":
                organizationStatsService.handleOrganizationDeleted();
                break;

            case "POSITION_CREATED":
                positionStatsService.handlePositionCreated();
                break;

            case "POSITION_DELETED":
                positionStatsService.handlePositionDeleted();
                break;

            case "LEAVE_APPROVED":
                LeaveDashboardDTO dataLeave = objectMapper.convertValue(event.getData(), LeaveDashboardDTO.class);
                leaveStatsService.handleLeaveApproved(dataLeave);
                break;

            case "LEAVE_CANCELLED":
                LeaveDashboardDTO cancelData = objectMapper.convertValue(event.getData(), LeaveDashboardDTO.class);
                leaveStatsService.handleLeaveCancelled(cancelData);
                break;

            case "ATTENDANCE_LATE":
                AttendanceDashboardDTO lateData = objectMapper.convertValue(event.getData(), AttendanceDashboardDTO.class);
                attendanceStatsService.handleEmployeeLate(lateData);
                break;

        }
    }

}
