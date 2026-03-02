package com.hrm.organization.controller;

import com.hrm.organization.dto.request.ReqCreatePositionDTO;
import com.hrm.organization.dto.response.ResPositionDTO;
import com.hrm.organization.dto.response.ResultPaginationDTO;
import com.hrm.organization.service.PositionService;
import com.hrm.organization.util.annotation.ApiMessage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/positions")
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;

    @PostMapping
    @PreAuthorize("hasAuthority('POSITION_CREATE')")
    @ApiMessage("Create position")
    public ResponseEntity<ResPositionDTO> create(
            @Valid @RequestBody ReqCreatePositionDTO req) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(positionService.create(req));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('POSITION_LIST')")
    @ApiMessage("Fetch list position")
    public ResponseEntity<ResultPaginationDTO> getAll(
            @RequestParam(required = false) UUID organizationId,
            @RequestParam(required = false) String search,
            Pageable pageable
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
                positionService.getAll(organizationId, search, pageable)
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('POSITION_VIEW')")
    @ApiMessage("Fetch position by id")
    public ResponseEntity<ResPositionDTO> getById(
            @PathVariable UUID id) {

        return ResponseEntity.status(HttpStatus.OK).body(
                positionService.getById(id)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('POSITION_DELETE')")
    @ApiMessage("Delete position")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {

        positionService.delete(id);

        return ResponseEntity.status(HttpStatus.OK).body(null);
    }
}