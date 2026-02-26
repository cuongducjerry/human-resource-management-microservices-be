package com.hrm.employee.controller;

import com.hrm.employee.dto.request.ReqCreateEmployee;
import com.hrm.employee.service.EmployeeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<String> createEmployee(
            @Valid @RequestBody ReqCreateEmployee request) {

        employeeService.createEmployee(request);
        return ResponseEntity.ok("Employee created successfully");
    }
}
