package com.hrm.auth.entity;

import com.hrm.auth.util.constant.Status;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "password_reset_requests")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Keycloak user ID
    private String keycloakId;

    // Employee ID in HRM database
    private UUID employeeId;

    private String email;

    @Enumerated(EnumType.STRING)
    private Status status;

    private String approvedBy;

    private Instant approvedAt;

}