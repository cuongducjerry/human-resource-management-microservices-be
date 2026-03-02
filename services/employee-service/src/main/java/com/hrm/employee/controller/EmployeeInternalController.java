package com.hrm.employee.controller;

import com.hrm.employee.repository.EmployeeRepository;
import com.hrm.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/internal/employees")
@RequiredArgsConstructor
public class EmployeeInternalController {

    private final EmployeeService employeeService;

    @GetMapping("/exists-by-organization/{orgId}")
    public ResponseEntity<Boolean> existsByOrganization(@PathVariable UUID orgId) {
        return ResponseEntity.ok(
                employeeService.existsByOrganization(orgId)
        );
    }

    @GetMapping("/exists-by-position/{positionId}")
    public ResponseEntity<Boolean> existsByPosition(@PathVariable UUID positionId) {
        return ResponseEntity.ok(
                employeeService.existsByPosition(positionId)
        );
    }

}
