package com.hrm.dashboard.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceDashboardDTO {
    private UUID employeeId;
    private UUID organizationId;
    private UUID positionId;
    private UUID managerId;
    private boolean late;
}
