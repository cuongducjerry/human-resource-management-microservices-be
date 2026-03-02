package com.hrm.employee.client;

import com.hrm.employee.config.FeignConfig;
import com.hrm.employee.dto.response.ResOrganizationDTO;
import com.hrm.employee.dto.response.ResPositionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name = "organization-service",
        configuration = FeignConfig.class
)
public interface OrganizationClient {

    @GetMapping("/api/internal/organizations/{id}")
    ResOrganizationDTO getOrganizationById(@PathVariable("id") UUID id);

    @GetMapping("/api/internal/positions/{id}")
    ResPositionDTO getPositionById(@PathVariable("id") UUID id);
}
