package com.hrm.employee.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResCreateEmployeeDTO {
    private UUID id;
    private String employeeCode;
    private String fullName;
    private String email;
    private UUID organizationId;
    private UUID positionId;
    private UUID managerId;
    private String keycloakUserId;
}