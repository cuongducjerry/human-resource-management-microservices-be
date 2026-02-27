package com.hrm.employee.controller;

import com.hrm.employee.dto.request.ReqCreateEmployee;
import com.hrm.employee.dto.response.ResCreateEmployee;
import com.hrm.employee.service.EmployeeService;

import com.hrm.employee.util.annotation.ApiMessage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    @PreAuthorize("hasAuthority('EMPLOYEE_CREATE')")
    @ApiMessage("Create a employee")
    public ResponseEntity<ResCreateEmployee> createEmployee(
            @Valid @RequestBody ReqCreateEmployee request) {

        ResCreateEmployee emp = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(emp);
    }
}
