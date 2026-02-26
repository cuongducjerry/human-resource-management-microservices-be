package com.hrm.employee.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class ReqCreateEmployee {

    @NotBlank
    private String fullName;

    @Email
    @NotBlank
    private String email;

    @NotNull
    private UUID departmentId;

    @NotNull
    private UUID positionId;
}
