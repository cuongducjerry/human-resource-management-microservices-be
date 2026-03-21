package com.hrm.organization.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PositionDashboardDTO {
    private UUID id;
    private UUID organizationId;
    private LocalDateTime createdAt;
}
