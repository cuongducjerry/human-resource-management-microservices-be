package com.hrm.employee.entity;

import com.hrm.employee.util.constant.EmployeeStatus;
import com.hrm.employee.util.constant.Gender;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "employees")
@SQLDelete(sql = "UPDATE employees SET active = false WHERE id = ?")
@Where(clause = "active = true")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    // ===== Keycloak Mapping =====
    @Column(name = "keycloak_user_id", nullable = false, unique = true)
    private String keycloakUserId;

    @Column(name = "employee_code", nullable = false, unique = true)
    private String employeeCode;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;

    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String address;

    private String avatarUrl;

    private LocalDate hireDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmployeeStatus status;

    // ===== Organization Reference (Microservice Style) =====
    private UUID organizationId;

    private UUID positionId;

    private UUID managerId;

    // ===== Lifecycle Tracking =====
    private LocalDate probationEndDate;

    private LocalDate confirmedDate;

    private LocalDate terminationDate;

    private String terminationReason;

    // ===== Soft Delete =====
    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    // ===== Auditing (Spring Data) =====
    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @CreatedBy
    @Column(updatable = false)
    private String createdBy;

    @LastModifiedBy
    private String updatedBy;
}
