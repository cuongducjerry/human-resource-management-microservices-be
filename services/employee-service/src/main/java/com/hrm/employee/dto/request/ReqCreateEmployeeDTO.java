package com.hrm.employee.dto.request;

import com.hrm.employee.util.constant.EmployeeStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ReqCreateEmployeeDTO {

    @NotBlank(message = "Full name must not be blank")
    private String fullName;

    @Email(message = "Email format is invalid")
    @NotBlank(message = "Email must not be blank")
    private String email;

    @NotNull(message = "Organization ID must not be null")
    private UUID organizationId;

    @NotNull(message = "Position ID must not be null")
    private UUID positionId;

    private UUID managerId;

    @NotEmpty(message = "Roles must not be empty")
    private List<String> roles;

    @NotNull(message = "Status must not be null")
    private EmployeeStatus status;
}
