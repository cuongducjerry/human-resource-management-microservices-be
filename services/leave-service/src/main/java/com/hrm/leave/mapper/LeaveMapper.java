package com.hrm.leave.mapper;

import com.hrm.leave.dto.response.ResLeaveBalanceDTO;
import com.hrm.leave.dto.response.ResLeaveRequestDTO;
import com.hrm.leave.entity.LeaveBalance;
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

    public ResLeaveBalanceDTO toBalanceDTO(LeaveBalance balance) {

        return ResLeaveBalanceDTO.builder()
                .id(balance.getId())
                .employeeId(balance.getEmployeeId())
                .leaveType(balance.getLeaveType())
                .totalDaysPerYear(balance.getTotalDaysPerYear())
                .usedDays(balance.getUsedDays())
                .remainingDays(balance.getRemainingDays())
                .year(balance.getYear())
                .createdAt(balance.getCreatedAt())
                .updatedAt(balance.getUpdatedAt())
                .createdBy(balance.getCreatedBy())
                .updatedBy(balance.getUpdatedBy())
                .build();
    }
}
