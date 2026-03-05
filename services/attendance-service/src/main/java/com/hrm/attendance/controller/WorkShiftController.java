package com.hrm.attendance.controller;

import com.hrm.attendance.dto.request.ReqCreateWorkShiftDTO;
import com.hrm.attendance.dto.request.ReqUpdateWorkShiftDTO;
import com.hrm.attendance.dto.response.ResWorkShiftDTO;
import com.hrm.attendance.dto.response.ResultPaginationDTO;
import com.hrm.attendance.service.WorkShiftService;
import com.hrm.attendance.util.annotation.ApiMessage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/work-shifts")
@RequiredArgsConstructor
public class WorkShiftController {

    private final WorkShiftService service;

    // ===== CREATE =====
    @PostMapping
    @PreAuthorize("hasAuthority('WORKSHIFT_CREATE')")
    @ApiMessage("Create work shift")
    public ResponseEntity<ResWorkShiftDTO> create(
            @Valid @RequestBody ReqCreateWorkShiftDTO request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    // ===== UPDATE =====
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('WORKSHIFT_UPDATE')")
    @ApiMessage("Update work shift")
    public ResponseEntity<ResWorkShiftDTO> update(
            @PathVariable UUID id,
            @Valid @RequestBody ReqUpdateWorkShiftDTO request) {

        return ResponseEntity.ok(service.update(id, request));
    }

    // ===== DELETE =====
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('WORKSHIFT_DELETE')")
    @ApiMessage("Delete work shift")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {

        service.delete(id);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    // ===== LIST =====
    @GetMapping
    @PreAuthorize("hasAuthority('WORKSHIFT_LIST')")
    @ApiMessage("Fetch work shift list")
    public ResponseEntity<ResultPaginationDTO> list(
            @RequestParam(required = false) String name,
            Pageable pageable) {

        return ResponseEntity.ok(service.list(name, pageable));
    }

    // ===== DETAIL =====
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('WORKSHIFT_VIEW')")
    @ApiMessage("Fetch work shift by id")
    public ResponseEntity<ResWorkShiftDTO> getById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(service.getById(id));
    }
}