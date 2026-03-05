package com.hrm.employee.client;

import com.hrm.employee.config.FeignConfig;
import com.hrm.employee.dto.response.ResWorkShiftDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name = "attendance-service",
        configuration = FeignConfig.class
)
public interface AttendanceClient {

    @GetMapping("/api/internal/work-shifts/{id}")
    ResWorkShiftDTO getWorkShiftById(@PathVariable("id") UUID id);
}
