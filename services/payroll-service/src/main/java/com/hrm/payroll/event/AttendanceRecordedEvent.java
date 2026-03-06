package com.hrm.payroll.event;

import com.hrm.payroll.util.constant.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRecordedEvent {

    private UUID employeeId;

    private LocalDate workDate;

    private Double totalHours;

    private Double overtimeHours;

    private AttendanceStatus status;

    private boolean earlyLeave;

    private boolean late;

}