package com.hrm.auth.controller;

import com.hrm.auth.dto.request.ReqCreateKeycloakUserDTO;
import com.hrm.auth.dto.request.ReqLoginDTO;
import com.hrm.auth.dto.request.ReqLogoutDTO;
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
            @CookieValue("refresh_token") String refreshToken
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

    @PostMapping("/logout")
    @ApiMessage("Logout User")
    public ResponseEntity<Void> logout(
            @CookieValue("refresh_token") String refreshToken
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

}
