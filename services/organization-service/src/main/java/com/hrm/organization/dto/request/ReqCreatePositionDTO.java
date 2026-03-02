package com.hrm.organization.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ReqCreatePositionDTO {

    @NotBlank(message = "Position name must not be blank")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Min(value = 1, message = "Level must be at least 1")
    @Max(value = 20, message = "Level must not exceed 20")
    private Integer level;

    @NotNull(message = "Organization ID must not be null")
    private UUID organizationId;
}
