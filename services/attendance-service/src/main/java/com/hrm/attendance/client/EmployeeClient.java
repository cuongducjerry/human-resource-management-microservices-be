package com.hrm.attendance.client;

import com.hrm.attendance.config.FeignConfig;
import com.hrm.attendance.dto.response.ResEmployeeDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@FeignClient(
        name = "employee-service",
        configuration = FeignConfig.class
)
public interface EmployeeClient {

    @GetMapping("/api/internal/employees/ids")
    List<UUID> getAllActiveEmployeeIds();

    @GetMapping("/api/internal/employees/{id}")
    ResEmployeeDTO getInternal(@PathVariable UUID id);

}
