package com.hrm.employee.client;

import com.hrm.employee.config.FeignConfig;
import com.hrm.employee.dto.request.ReqCreateKeycloakUserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "auth-service", configuration = FeignConfig.class)
public interface AuthClient {

    @PostMapping("/api/auth/create-user")
    String createUser(@RequestBody ReqCreateKeycloakUserDTO request);

    @PostMapping("/api/auth/delete-user")
    void deleteUser(@RequestBody String userId);
}
