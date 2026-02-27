package com.hrm.employee.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ReqCreateEmployee {

    @NotBlank(message = "Full name must not be blank")
    private String fullName;

    @Email(message = "Email format is invalid")
    @NotBlank(message = "Email must not be blank")
    private String email;

    @NotNull(message = "Department ID must not be null")
    private UUID departmentId;

    @NotNull(message = "Position ID must not be null")
    private UUID positionId;

    @NotEmpty(message = "Roles must not be empty")
    private List<String> roles;

}
