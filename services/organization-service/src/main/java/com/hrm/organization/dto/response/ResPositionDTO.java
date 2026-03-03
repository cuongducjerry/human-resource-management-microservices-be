package com.hrm.organization.dto.response;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ResPositionDTO {

    private UUID id;

    private String name;

    private String description;

    private Integer level;

    private UUID organizationId;

    private Instant createdAt;
    private Instant updatedAt;

    private String createdBy;
    private String updatedBy;
}
