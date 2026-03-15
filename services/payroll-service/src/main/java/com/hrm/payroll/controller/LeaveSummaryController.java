package com.hrm.payroll.controller;

import com.hrm.payroll.dto.response.ResLeaveSummaryDTO;
import com.hrm.payroll.dto.response.ResultPaginationDTO;
import com.hrm.payroll.service.LeaveSummaryService;
import com.hrm.payroll.util.annotation.ApiMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/leave-summary")
@RequiredArgsConstructor
public class LeaveSummaryController {

    private final LeaveSummaryService service;

    // ================= LIST =================
    @GetMapping
    @PreAuthorize("hasAuthority('LEAVE_SUMMARY_LIST')")
    @ApiMessage("Fetch leave summary list")
    public ResponseEntity<ResultPaginationDTO> list(
            @RequestParam(required = false) UUID employeeId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.list(employeeId, month, year, pageable));
    }

    @GetMapping("/personal")
    @PreAuthorize("hasAuthority('LEAVE_SUMMARY_LIST_PERSONAL')")
    @ApiMessage("Fetch leave summary list (employee, manager)")
    public ResponseEntity<ResultPaginationDTO> listSummaryPersonal(
            @RequestParam(required = false) UUID employeeId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.listLeaveSummaryPersonal(employeeId, month, year, pageable));
    }

    // ================= DETAIL =================
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('LEAVE_SUMMARY_VIEW')")
    @ApiMessage("Fetch leave summary detail")
    public ResponseEntity<ResLeaveSummaryDTO> getById(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(service.getById(id));
    }
}
