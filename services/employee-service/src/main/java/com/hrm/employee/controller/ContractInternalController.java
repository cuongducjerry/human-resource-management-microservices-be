package com.hrm.employee.controller;

import com.hrm.employee.dto.response.ResContractDTO;
import com.hrm.employee.service.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/internal/contracts")
@RequiredArgsConstructor
public class ContractInternalController {

    private final ContractService contractService;

    // lấy contract active của employee
    @GetMapping("/employee/{employeeId}/active")
    public ResContractDTO getActiveContractByEmployee(@PathVariable UUID employeeId) {
        return contractService.getActiveContractByEmployee(employeeId);
    }

}
