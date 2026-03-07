package com.hrm.payroll.client;

import com.hrm.payroll.config.FeignConfig;
import com.hrm.payroll.dto.response.ResContractDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "employee-service", configuration = FeignConfig.class)
public interface EmployeeClient {

    @GetMapping("/api/internal/employees/ids")
    List<UUID> getAllActiveEmployeeIds();

    @GetMapping("/api/internal/contracts/employee/{employeeId}/active")
    ResContractDTO getActiveContract(@PathVariable UUID employeeId);

}
