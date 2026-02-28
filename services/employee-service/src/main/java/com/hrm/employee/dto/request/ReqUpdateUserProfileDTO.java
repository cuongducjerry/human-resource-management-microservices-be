package com.hrm.employee.dto.request;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class ReqUpdateUserProfileDTO {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;
}
