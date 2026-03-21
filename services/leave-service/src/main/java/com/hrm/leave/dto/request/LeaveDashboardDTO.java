package com.hrm.leave.dto.request;

import com.hrm.leave.util.constant.LeaveType;
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
public class LeaveDashboardDTO {

    private UUID employeeId;
    private UUID organizationId;
    private UUID positionId;
    private UUID managerId;
    private LeaveType leaveType;
    private Integer totalDays;
    private LocalDate startDate;
    private LocalDate endDate;
}
