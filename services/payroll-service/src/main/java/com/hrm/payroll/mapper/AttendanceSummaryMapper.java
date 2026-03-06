package com.hrm.payroll.mapper;

import com.hrm.payroll.dto.response.ResAttendanceSummaryDTO;
import com.hrm.payroll.entity.AttendanceSummary;
import org.springframework.stereotype.Component;

@Component
public class AttendanceSummaryMapper {

    public ResAttendanceSummaryDTO toDTO(AttendanceSummary entity) {
        return ResAttendanceSummaryDTO.builder()
                .id(entity.getId())
                .employeeId(entity.getEmployeeId())
                .month(entity.getMonth())
                .year(entity.getYear())
                .workingDays(entity.getWorkingDays())
                .lateDays(entity.getLateDays())
                .absentDays(entity.getAbsentDays())
                .totalWorkHours(entity.getTotalWorkHours())
                .overtimeHours(entity.getOvertimeHours())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

}
