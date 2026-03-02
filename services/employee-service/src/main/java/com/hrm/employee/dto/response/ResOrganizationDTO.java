package com.hrm.employee.dto.response;

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

    private String status;

    private Instant createdAt;
    private Instant updatedAt;

    private String createdBy;
    private String updatedBy;
}