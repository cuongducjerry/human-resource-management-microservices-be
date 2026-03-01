package com.hrm.employee.client;

import com.hrm.employee.config.FeignConfig;
import com.hrm.employee.dto.request.ReqCreateKeycloakUserDTO;
import com.hrm.employee.dto.request.ReqUpdateUserProfileDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "auth-service", configuration = FeignConfig.class)
public interface AuthClient {

    @PostMapping("/api/auth/create-user")
    String createUser(@RequestBody ReqCreateKeycloakUserDTO request);

    @DeleteMapping("/api/auth/users/{id}")
    void deleteUser(@PathVariable("id") String id);

    @PutMapping("/api/auth/users/{id}/disable")
    void disableUser(@PathVariable("id") String id);

    @PutMapping("/api/auth/users/{userId}/profile")
    void updateUserProfile(
            @PathVariable("userId") String userId,
            @RequestBody ReqUpdateUserProfileDTO request
    );

    @PutMapping("/api/auth/users/{id}/enable")
    void enableUser(@PathVariable("id") String id);

    @GetMapping("/api/auth/users/{id}/roles")
    List<String> getUserRoles(@PathVariable("id") String id);

    @PutMapping("/api/auth/users/{id}/roles")
    void updateUserRoles(
            @PathVariable("id") String id,
            @RequestBody List<String> roles
    );

}
