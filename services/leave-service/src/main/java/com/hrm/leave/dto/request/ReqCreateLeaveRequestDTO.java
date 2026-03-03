package com.hrm.leave.dto.request;

import com.hrm.leave.util.constant.LeaveType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class ReqCreateLeaveRequestDTO {

    @NotNull
    private UUID employeeId;

    @NotNull
    private LeaveType leaveType;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    private String reason;
}
