package com.hrm.organization.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ReqCreateOrganizationDTO {

    @NotBlank(message = "Organization code must not be blank")
    private String code;

    @NotBlank(message = "Organization name must not be blank")
    private String name;

    private UUID parentId;
}
