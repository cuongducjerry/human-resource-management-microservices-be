package com.hrm.employee.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.UUID;

@FeignClient(name = "leave-service")
public interface LeaveClient {

    @PostMapping("/api/internal/leave-balances/init/{employeeId}")
    void initLeaveBalance(@PathVariable UUID employeeId);

}
