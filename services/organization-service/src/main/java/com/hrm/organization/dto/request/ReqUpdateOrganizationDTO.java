package com.hrm.organization.dto.request;

import com.hrm.organization.util.constant.OrganizationStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ReqUpdateOrganizationDTO {

    @NotBlank(message = "Organization code must not be blank")
    private String code;

    @NotBlank(message = "Organization name must not be blank")
    private String name;

    private UUID parentId;

    private OrganizationStatus status;
}
