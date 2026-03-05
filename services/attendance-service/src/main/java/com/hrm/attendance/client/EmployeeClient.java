package com.hrm.attendance.client;

import com.hrm.attendance.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.UUID;

@FeignClient(
        name = "employee-service",
        configuration = FeignConfig.class
)
public interface EmployeeClient {

    @GetMapping("/ids")
    List<UUID> getAllActiveEmployeeIds();
}
