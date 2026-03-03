package com.hrm.leave.dto.response;

import com.hrm.leave.util.constant.LeaveStatus;
import com.hrm.leave.util.constant.LeaveType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class ResLeaveRequestDTO {

    private UUID id;

    private UUID employeeId;

    private LeaveType leaveType;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer totalDays;

    private LeaveStatus status;

    private String approverId;

    private String reason;

    private Instant createdAt;

    private Instant updatedAt;

    private String createdBy;

    private String updatedBy;


}
