package com.hrm.auth.client;


import com.hrm.auth.config.FeignConfig;
import com.hrm.auth.dto.response.ResEmployeeDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "employee-service", configuration = FeignConfig.class)
public interface EmployeeClient {

    @GetMapping("/api/internal/employees/{id}")
    ResEmployeeDTO getInternal(@PathVariable UUID id);

}
