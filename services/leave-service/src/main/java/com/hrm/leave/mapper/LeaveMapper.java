package com.hrm.leave.mapper;

import com.hrm.leave.dto.response.ResLeaveRequestDTO;
import com.hrm.leave.entity.LeaveRequest;
import org.springframework.stereotype.Component;

@Component
public class LeaveMapper {

    public ResLeaveRequestDTO toDTO(LeaveRequest leave) {
        return ResLeaveRequestDTO.builder()
                .id(leave.getId())
                .employeeId(leave.getEmployeeId())
                .leaveType(leave.getLeaveType())
                .startDate(leave.getStartDate())
                .endDate(leave.getEndDate())
                .totalDays(leave.getTotalDays())
                .status(leave.getStatus())
                .approverId(leave.getApproverId())
                .reason(leave.getReason())
                .createdAt(leave.getCreatedAt())
                .updatedAt(leave.getUpdatedAt())
                .createdBy(leave.getCreatedBy())
                .updatedBy(leave.getUpdatedBy())
                .build();
    }
}
