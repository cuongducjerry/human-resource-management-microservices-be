package com.hrm.organization.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "employee-service")
public interface EmployeeClient {

    @GetMapping("/api/internal/employees/exists-by-organization/{orgId}")
    Boolean existsByOrganization(@PathVariable UUID orgId);

    @GetMapping("/api/internal/employees/exists-by-position/{positionId}")
    Boolean existsByPosition(@PathVariable UUID positionId);

}