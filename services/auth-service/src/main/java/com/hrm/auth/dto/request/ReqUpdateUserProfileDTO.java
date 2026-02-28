package com.hrm.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReqUpdateUserProfileDTO {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;
}
