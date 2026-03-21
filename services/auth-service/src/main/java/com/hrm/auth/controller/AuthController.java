package com.hrm.auth.controller;

import com.hrm.auth.dto.request.*;
import com.hrm.auth.dto.response.ResLoginDTO;
import com.hrm.auth.dto.response.ResultPaginationDTO;
import com.hrm.auth.entity.PasswordResetRequest;
import com.hrm.auth.service.AuthService;
import com.hrm.auth.specification.PasswordResetRequestSpecification;
import com.hrm.auth.util.annotation.ApiMessage;
import com.hrm.auth.util.constant.Status;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


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
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(res);
    }

    @GetMapping("/account")
    @ApiMessage("Fetch account")
    public ResponseEntity<ResLoginDTO.UserAccount> getAccount() {
        ResLoginDTO.UserAccount userGetAccount = this.authService.getUserAccount();
        return ResponseEntity.ok().body(userGetAccount);
    }

    @GetMapping("/refresh")
    @ApiMessage("Get User by refresh token")
    public ResponseEntity<ResLoginDTO> refresh(
            @CookieValue(value = "refresh_token", required = false)
            String refreshToken
    ) {

        System.out.println("======================== RECEIVED REFRESH TOKEN ==========================");
        System.out.println(refreshToken);

        ResLoginDTO result = authService.refresh(refreshToken);

        ResponseCookie cookie = ResponseCookie
                .from("refresh_token", result.getRefreshToken())
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
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

    @GetMapping("/internal/users/{id}/roles")
    @ApiMessage("Get user roles")
    public List<String> getUserRoles(@PathVariable String id) {

        return authService.getUserRoles(id);
    }

    @PutMapping("/internal/users/{id}/roles")
    @ApiMessage("Update user roles")
    public void updateUserRoles(
            @PathVariable String id,
            @RequestBody List<String> roles
    ) {

        authService.updateUserRoles(id, roles);

    }

    @PutMapping("/internal/users/{id}/change-password")
    @ApiMessage("Change user password")
    public void changePassword(
            @PathVariable String id,
            @Valid @RequestBody ReqChangePasswordDTO request
    ) {

        authService.changePassword(
                id,
                request.getCurrentPassword(),
                request.getNewPassword()
        );

    }

    @GetMapping("/internal/users/by-role/{role}")
    public List<String> getUserIdsByRole(
            @PathVariable String role
    ) {
        return authService.getUserIdsByRole(role);
    }

    @PostMapping("/forgot-password")
    @ApiMessage("Submit forgot password request")
    public ResponseEntity<Void> forgotPassword(
            @RequestBody ReqForgotPasswordDTO request
    ) {

        authService.createResetRequest(request.getEmail());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/reset-requests")
    @PreAuthorize("hasAuthority('RESET_PASSWORD_LIST')")
    @ApiMessage("Fetch password reset requests")
    public ResponseEntity<ResultPaginationDTO> getAllResetPasswords(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Status status,
            Pageable pageable
    ) {

        Specification<PasswordResetRequest> spec =
                PasswordResetRequestSpecification.filter(keyword, status);

        return ResponseEntity.status(HttpStatus.OK).body(
                authService.handleListResetRequests(spec, pageable)
        );
    }

    @PostMapping("/reset-requests/{id}/approve")
    @PreAuthorize("hasAuthority('RESET_PASSWORD_APPROVE')")
    @ApiMessage("Approve password reset request")
    public ResponseEntity<Void> approveReset(@PathVariable UUID id) {
        authService.approveResetRequest(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-requests/{id}/reject")
    @PreAuthorize("hasAuthority('RESET_PASSWORD_REJECT')")
    @ApiMessage("Reject password reset request")
    public ResponseEntity<Void> rejectReset(@PathVariable UUID id) {
        authService.rejectResetRequest(id);
        return ResponseEntity.ok().build();
    }

}
