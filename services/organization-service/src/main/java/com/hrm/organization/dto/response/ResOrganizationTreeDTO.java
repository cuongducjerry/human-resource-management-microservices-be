package com.hrm.organization.dto.response;

import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResOrganizationTreeDTO {

    private UUID id;
    private String name;

    @Builder.Default
    private List<ResOrganizationTreeDTO> children = new ArrayList<>();
}