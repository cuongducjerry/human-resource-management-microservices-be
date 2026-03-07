package com.hrm.payroll.dto.response;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResLeaveSummaryDTO {

    private UUID id;

    private UUID employeeId;

    private Integer month;

    private Integer year;

    private Integer paidLeaveDays;

    private Integer unpaidLeaveDays;

    private boolean active;

    private Instant createdAt;

    private Instant updatedAt;
}
