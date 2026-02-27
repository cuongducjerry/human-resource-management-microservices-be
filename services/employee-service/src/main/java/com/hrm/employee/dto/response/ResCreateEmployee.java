package com.hrm.employee.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ResCreateEmployee {

    private UUID id;

    private String employeeCode;

    private String fullName;

    private String email;

    private UUID departmentId;

    private UUID positionId;

    private String keycloakUserId;
}