package com.hrm.payroll.controller;

import com.hrm.payroll.dto.response.ResAttendanceSummaryDTO;
import com.hrm.payroll.dto.response.ResultPaginationDTO;
import com.hrm.payroll.service.AttendanceSummaryService;
import com.hrm.payroll.util.annotation.ApiMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/attendance-summary")
@RequiredArgsConstructor
public class AttendanceSummaryController {

    private final AttendanceSummaryService service;

    // ================= LIST =================
    @GetMapping
    @PreAuthorize("hasAuthority('ATTENDANCE_SUMMARY_LIST')")
    @ApiMessage("Fetch attendance summary list")
    public ResponseEntity<ResultPaginationDTO> list(
            @RequestParam(required = false) UUID employeeId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.list(employeeId, month, year, pageable));
    }

    // ================= DETAIL =================
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ATTENDANCE_SUMMARY_VIEW')")
    @ApiMessage("Fetch attendance summary detail")
    public ResponseEntity<ResAttendanceSummaryDTO> getById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(service.getById(id));
    }
}