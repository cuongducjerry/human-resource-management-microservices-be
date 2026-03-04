package com.hrm.leave.client;

import com.hrm.leave.config.FeignConfig;
import com.hrm.leave.dto.response.ResEmployeeDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "employee-service", configuration = FeignConfig.class)
public interface EmployeeClient {

    @GetMapping("/api/internal/employees/{id}/exists")
    Boolean existsById(@PathVariable UUID id);

    @GetMapping("/api/internal/employees/{id}")  
    ResEmployeeDTO getInternal(@PathVariable UUID id);

}
