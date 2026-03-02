package com.hrm.organization.mapper;

import com.hrm.organization.dto.response.ResPositionDTO;
import com.hrm.organization.entity.Position;
import org.springframework.stereotype.Component;

@Component
public class PositionMapper {

    public ResPositionDTO convertToResPositionDTO(Position entity) {
        return ResPositionDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .level(entity.getLevel())
                .organizationId(entity.getOrganizationId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }
}
