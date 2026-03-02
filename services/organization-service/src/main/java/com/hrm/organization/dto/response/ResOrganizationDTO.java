package com.hrm.organization.dto.response;

import com.hrm.organization.util.constant.OrganizationStatus;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResOrganizationDTO {

    private UUID id;

    private String code;

    private String name;

    private UUID parentId;

    private OrganizationStatus status;

    private Instant createdAt;
    private Instant updatedAt;

    private String createdBy;
    private String updatedBy;
}