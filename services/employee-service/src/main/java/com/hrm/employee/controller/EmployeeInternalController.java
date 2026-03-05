package com.hrm.employee.controller;

import com.hrm.employee.dto.response.ResEmployeeDTO;
import com.hrm.employee.repository.EmployeeRepository;
import com.hrm.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/internal/employees")
@RequiredArgsConstructor
public class EmployeeInternalController {

    private final EmployeeService employeeService;

    @GetMapping("/{id}/exists")
    public Boolean existsById(@PathVariable UUID id) {
        return employeeService.existsById(id);
    }

    @GetMapping("/exists-by-organization/{orgId}")
    public Boolean existsByOrganization(@PathVariable UUID orgId) {
        return employeeService.existsByOrganization(orgId);
    }

    @GetMapping("/exists-by-position/{positionId}")
    public Boolean existsByPosition(@PathVariable UUID positionId) {
        return employeeService.existsByPosition(positionId);
    }

    @GetMapping("/{id}")
    public ResEmployeeDTO getInternal(@PathVariable UUID id) {
        return employeeService.getEmployeeById(id);
    }

    @GetMapping("/ids")
    public List<UUID> getAllActiveEmployeeIds() {
        return employeeService.getAllActiveEmployeeIds();
    }


}
