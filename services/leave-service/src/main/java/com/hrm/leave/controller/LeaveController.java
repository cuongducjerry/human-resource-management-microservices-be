package com.hrm.leave.controller;

import com.hrm.leave.dto.request.ReqCreateLeaveRequestDTO;
import com.hrm.leave.dto.response.ResLeaveBalanceDTO;
import com.hrm.leave.dto.response.ResLeaveRequestDTO;
import com.hrm.leave.dto.response.ResultPaginationDTO;
import com.hrm.leave.entity.LeaveRequest;
import com.hrm.leave.service.LeaveService;
import com.hrm.leave.specification.LeaveSpecification;
import com.hrm.leave.util.annotation.ApiMessage;
import com.hrm.leave.util.constant.LeaveStatus;
import com.hrm.leave.util.constant.LeaveType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    @PostMapping
    @PreAuthorize("hasAuthority('LEAVE_CREATE')")
    @ApiMessage("Create leave request")
    public ResponseEntity<ResLeaveRequestDTO> create(
            @Valid @RequestBody ReqCreateLeaveRequestDTO req) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(leaveService.create(req));
    }

    // ================= LIST =================
    @GetMapping
    @PreAuthorize("hasAuthority('LEAVE_REQUEST_LIST')")
    @ApiMessage("Fetch leave request list")
    public ResponseEntity<ResultPaginationDTO> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) LeaveStatus status,
            @RequestParam(required = false) LeaveType type,
            Pageable pageable
    ) {

        Specification<LeaveRequest> spec =
                LeaveSpecification.filter(keyword, status, type);

        return ResponseEntity.ok(
                leaveService.handleListLeaveRequest(spec, pageable)
        );
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('LEAVE_APPROVE')")
    @ApiMessage("Approve leave request")
    public ResponseEntity<Void> approve(@PathVariable UUID id) {

        leaveService.approve(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('LEAVE_REJECT')")
    @ApiMessage("Reject leave request")
    public ResponseEntity<Void> reject(
            @PathVariable UUID id) {

        leaveService.reject(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('LEAVE_CANCEL')")
    @ApiMessage("Cancel leave request")
    public ResponseEntity<Void> cancel(
            @PathVariable UUID id) {

        leaveService.cancel(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{employeeId}/leave-balances")
    @PreAuthorize("hasAuthority('LEAVE_BALANCE_VIEW')")
    @ApiMessage("Get leave balance by employee")
    public ResponseEntity<List<ResLeaveBalanceDTO>> getByEmployee(
            @PathVariable UUID employeeId) {

        return ResponseEntity.ok(
                leaveService.getBalanceByEmployee(employeeId)
        );
    }
}
