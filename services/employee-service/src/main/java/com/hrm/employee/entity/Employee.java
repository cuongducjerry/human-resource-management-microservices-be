package com.hrm.employee.entity;

import com.hrm.employee.util.constant.EmployeeStatus;
import com.hrm.employee.util.constant.Gender;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Map with Keycloak user (sub-in JWT)
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

    private LocalDate hireDate;

    @Enumerated(EnumType.STRING)
    private EmployeeStatus status;

    // Microservice style: only stores ID, not @ManyToOne
    private UUID departmentId;

    private UUID positionId;
}
