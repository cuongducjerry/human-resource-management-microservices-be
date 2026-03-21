package com.hrm.employee.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDashboardDTO {

    private UUID id;
    private String gender;
    private UUID organizationId;
    private UUID positionId;
    private UUID managerId;
}
