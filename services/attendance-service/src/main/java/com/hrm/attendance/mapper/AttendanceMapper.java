package com.hrm.attendance.mapper;

import com.hrm.attendance.dto.response.ResAttendanceDTO;
import com.hrm.attendance.entity.Attendance;
import org.springframework.stereotype.Component;

@Component
public class AttendanceMapper {

    public ResAttendanceDTO toDTO(Attendance a) {

        return ResAttendanceDTO.builder()
                .id(a.getId())
                .employeeId(a.getEmployeeId())
                .workDate(a.getWorkDate())
                .checkInTime(a.getCheckInTime())
                .checkOutTime(a.getCheckOutTime())
                .totalHours(a.getTotalHours())
                .overtimeHours(a.getOvertimeHours())
                .status(a.getStatus())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .createdBy(a.getCreatedBy())
                .updatedBy(a.getUpdatedBy())
                .build();
    }
}
