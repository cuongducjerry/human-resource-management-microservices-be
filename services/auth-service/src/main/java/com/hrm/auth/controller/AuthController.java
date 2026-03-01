package com.hrm.auth.controller;

import com.hrm.auth.dto.request.ReqCreateKeycloakUserDTO;
import com.hrm.auth.dto.request.ReqLoginDTO;
import com.hrm.auth.dto.request.ReqLogoutDTO;
import com.hrm.auth.dto.request.ReqUpdateUserProfileDTO;
import com.hrm.auth.dto.response.ResLoginDTO;
import com.hrm.auth.service.AuthService;
import com.hrm.auth.util.annotation.ApiMessage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    @PostMapping("/login")
    public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody ReqLoginDTO request) {

        ResLoginDTO res = authService.login(
                request.getUsername(),
                request.getPassword()
        );

        ResponseCookie cookie = ResponseCookie
                .from("refresh_token", res.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(res);
    }

    @GetMapping("/refresh")
    @ApiMessage("Get User by refresh token")
    public ResponseEntity<ResLoginDTO> refresh(
            @CookieValue(value = "refresh_token", required = false)
            String refreshToken
    ) {

        ResLoginDTO result = authService.refresh(refreshToken);

        ResponseCookie cookie = ResponseCookie
                .from("refresh_token", result.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(result);
    }

    @PostMapping("/create-user")
    public ResponseEntity<String> createUser(
            @RequestBody ReqCreateKeycloakUserDTO request
    ) {
        String userId = authService.createUser(request);
        return ResponseEntity.ok(userId);
    }

    @PutMapping("/users/{userId}/profile")
    public ResponseEntity<Void> updateUserProfile(
            @PathVariable String userId,
            @Valid @RequestBody ReqUpdateUserProfileDTO request
    ) {

        authService.updateUserProfile(
                userId,
                request.getFirstName(),
                request.getLastName()
        );

        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{id}/disable")
    @ApiMessage("Disable user")
    public ResponseEntity<Void> disableUser(
            @PathVariable String id
    ) {
        authService.disableUser(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    @ApiMessage("Logout User")
    public ResponseEntity<Void> logout(
            @CookieValue(value = "refresh_token", required = false)
            String refreshToken
    ) {

        authService.logout(refreshToken);

        ResponseCookie deleteCookie = ResponseCookie
                .from("refresh_token", null)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .build();
    }

    @DeleteMapping("/users/{id}")
    @ApiMessage("Delete user permanently")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {

        authService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{id}/enable")
    @ApiMessage("Enable user")
    public ResponseEntity<Void> enableUser(@PathVariable String id) {

        authService.enableUser(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users/{id}/roles")
    @ApiMessage("Get user roles")
    public ResponseEntity<List<String>> getUserRoles(@PathVariable String id) {

        return ResponseEntity.ok(authService.getUserRoles(id));
    }

    @PutMapping("/users/{id}/roles")
    @ApiMessage("Update user roles")
    public ResponseEntity<Void> updateUserRoles(
            @PathVariable String id,
            @RequestBody List<String> roles
    ) {

        authService.updateUserRoles(id, roles);
        return ResponseEntity.ok().build();
    }

}
