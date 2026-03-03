package com.hrm.leave.controller;

import com.hrm.leave.service.LeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/internal/leave-balances")
@RequiredArgsConstructor
public class LeaveBalanceInternalController {

    private final LeaveService leaveService;

    @PostMapping("/init/{employeeId}")
    public ResponseEntity<Void> initLeaveBalance(
            @PathVariable UUID employeeId) {

        leaveService.initLeaveBalance(employeeId);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
