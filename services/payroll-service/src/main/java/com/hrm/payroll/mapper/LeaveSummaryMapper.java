package com.hrm.payroll.mapper;

import com.hrm.payroll.dto.response.ResLeaveSummaryDTO;
import com.hrm.payroll.entity.LeaveSummary;
import org.springframework.stereotype.Component;

@Component
public class LeaveSummaryMapper {

    public ResLeaveSummaryDTO toDTO(LeaveSummary entity) {

        return ResLeaveSummaryDTO.builder()
                .id(entity.getId())
                .employeeId(entity.getEmployeeId())
                .month(entity.getMonth())
                .year(entity.getYear())
                .paidLeaveDays(entity.getPaidLeaveDays())
                .unpaidLeaveDays(entity.getUnpaidLeaveDays())
                .active(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
