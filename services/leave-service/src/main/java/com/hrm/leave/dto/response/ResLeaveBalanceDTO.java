package com.hrm.leave.dto.response;

import com.hrm.leave.util.constant.LeaveType;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResLeaveBalanceDTO {

    private UUID id;

    private UUID employeeId;

    private LeaveType leaveType;

    private Integer totalDaysPerYear;

    private Integer usedDays;

    private Integer remainingDays;

    private Integer year;

    private Instant createdAt;

    private Instant updatedAt;

    private String createdBy;

    private String updatedBy;
}
