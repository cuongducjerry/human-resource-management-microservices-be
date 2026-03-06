package com.hrm.payroll.dto.response;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResAttendanceSummaryDTO {

    private UUID id;

    private UUID employeeId;

    private Integer month;

    private Integer year;

    private Integer workingDays;

    private Integer lateDays;

    private Integer absentDays;

    private Double totalWorkHours;

    private Double overtimeHours;

    private Instant createdAt;

    private Instant updatedAt;
}