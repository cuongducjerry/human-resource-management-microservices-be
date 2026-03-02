package com.hrm.organization.controller;

import com.hrm.organization.dto.request.ReqCreateOrganizationDTO;
import com.hrm.organization.dto.request.ReqUpdateOrganizationDTO;
import com.hrm.organization.dto.response.ResOrganizationDTO;
import com.hrm.organization.dto.response.ResultPaginationDTO;
import com.hrm.organization.service.OrganizationService;
import com.hrm.organization.util.annotation.ApiMessage;
import com.hrm.organization.util.constant.OrganizationStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @PostMapping
    @PreAuthorize("hasAuthority('ORG_CREATE')")
    @ApiMessage("Create organization")
    public ResponseEntity<ResOrganizationDTO> create(
            @Valid @RequestBody ReqCreateOrganizationDTO req) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(organizationService.create(req));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ORG_LIST')")
    @ApiMessage("Fetch list organization")
    public ResponseEntity<ResultPaginationDTO> getAll(
            @RequestParam(required = false) UUID id,
            @RequestParam(required = false) OrganizationStatus status,
            @RequestParam(required = false) String search,
            Pageable pageable
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
                organizationService.getAll(id, status, search, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ORG_VIEW')")
    @ApiMessage("Fetch organization by id")
    public ResponseEntity<ResOrganizationDTO> getOrganizationById(@PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.OK).body(
                organizationService.getById(id)
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ORG_UPDATE')")
    @ApiMessage("Update organization")
    public ResponseEntity<ResOrganizationDTO> update(
            @PathVariable UUID id,
            @Valid @RequestBody ReqUpdateOrganizationDTO req) {

        return ResponseEntity.status(HttpStatus.OK).body(
                organizationService.update(id, req)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ORG_DELETE')")
    @ApiMessage("Delete organization")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        organizationService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

}
