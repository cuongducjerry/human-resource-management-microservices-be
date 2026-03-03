package com.hrm.employee.client;

import com.hrm.employee.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.UUID;

@FeignClient(name = "leave-service", configuration = FeignConfig.class)
public interface LeaveClient {

    @PostMapping("/api/internal/leave-balances/init/{employeeId}")
    void initLeaveBalance(@PathVariable UUID employeeId);

}
