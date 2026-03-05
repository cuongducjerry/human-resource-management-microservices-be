package com.hrm.attendance.dto.response;

import com.hrm.attendance.util.constant.AttendanceStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class ResAttendanceDTO {

    private UUID id;

    private UUID employeeId;

    private LocalDate workDate;

    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;

    private Double totalHours;
    private Double overtimeHours;

    private AttendanceStatus status;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
