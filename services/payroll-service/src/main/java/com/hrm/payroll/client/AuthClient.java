package com.hrm.payroll.client;

import com.hrm.payroll.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "auth-service", configuration = FeignConfig.class)
public interface AuthClient {

    @GetMapping("/api/auth/internal/users/{id}/roles")
    public List<String> getUserRoles(@PathVariable String id);

}
