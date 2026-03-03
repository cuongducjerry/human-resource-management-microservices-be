package com.hrm.organization.controller;

import com.hrm.organization.dto.response.ResOrganizationDTO;
import com.hrm.organization.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/internal/organizations")
@RequiredArgsConstructor
public class OrganizationInternalController {

    private final OrganizationService organizationService;

    @GetMapping("/{id}")
    public ResOrganizationDTO getOrganizationById(@PathVariable UUID id) {
        return organizationService.getById(id);
    }
}