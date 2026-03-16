package com.hrm.payroll.controller;

import com.hrm.payroll.dto.response.ResPayrollDTO;
import com.hrm.payroll.dto.response.ResultPaginationDTO;
import com.hrm.payroll.service.PayrollService;
import com.hrm.payroll.util.annotation.ApiMessage;
import com.hrm.payroll.util.constant.PayrollStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payrolls")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;

    @GetMapping
    @PreAuthorize("hasAuthority('PAYROLL_LIST')")
    @ApiMessage("Fetch payroll list")
    public ResponseEntity<ResultPaginationDTO> list(
            @RequestParam(required = false) UUID employeeId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) PayrollStatus status,
            Pageable pageable
    ) {

        return ResponseEntity.ok(
                payrollService.list(employeeId, month, year, status, pageable)
        );
    }

    @GetMapping("/personal")
    @PreAuthorize("hasAuthority('PAYROLL_LIST_PERSONAL')")
    @ApiMessage("Fetch payroll list (employee, manager)")
    public ResponseEntity<ResultPaginationDTO> listPayrollPersonal(
            @RequestParam(required = false) UUID employeeId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) PayrollStatus status,
            Pageable pageable
    ) {

        return ResponseEntity.ok(
                payrollService.listPersonal(employeeId, month, year, status, pageable)
        );
    }

    @PostMapping("/calculate-all")
    @PreAuthorize("hasAuthority('PAYROLL_CALCULATE')")
    @ApiMessage("Calculate all payroll")
    public ResponseEntity<Void> calculateAll(
            @RequestParam Integer month,
            @RequestParam Integer year
    ) {

        payrollService.calculateAll(month, year);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/paid-all")
    @PreAuthorize("hasAuthority('PAYROLL_PAY')")
    @ApiMessage("Mark all payroll as paid")
    public ResponseEntity<Void> paidAll(
            @RequestParam Integer month,
            @RequestParam Integer year
    ) {

        payrollService.markAllAsPaid(month, year);
        return ResponseEntity.ok().build();
    }

    // Generate monthly payroll
    @PostMapping("/generate")
    @PreAuthorize("hasAuthority('PAYROLL_GENERATE')")
    @ApiMessage("Generate payroll for month")
    public ResponseEntity<List<ResPayrollDTO>> generatePayroll(
            @RequestParam int month,
            @RequestParam int year
    ) {

        return ResponseEntity.ok(
                payrollService.generatePayrollForMonth(month, year)
        );
    }

    // payroll detail
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PAYROLL_VIEW')")
    @ApiMessage("Fetch payroll detail")
    public ResponseEntity<ResPayrollDTO> getPayrollDetail(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                payrollService.getPayrollDetail(id)
        );
    }

    // calculate payroll
    @PostMapping("/{id}/calculate")
    @PreAuthorize("hasAuthority('PAYROLL_CALCULATE')")
    @ApiMessage("Calculate payroll")
    public ResponseEntity<ResPayrollDTO> calculatePayroll(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                payrollService.calculatePayroll(id)
        );
    }

    // pay salary
    @PostMapping("/{id}/paid")
    @PreAuthorize("hasAuthority('PAYROLL_PAY')")
    @ApiMessage("Mark payroll as paid")
    public ResponseEntity<ResPayrollDTO> payPayroll(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                payrollService.markAsPaid(id)
        );
    }

}
