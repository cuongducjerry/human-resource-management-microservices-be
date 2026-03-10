package com.hrm.payroll.dto.response;

import com.hrm.payroll.util.constant.Gender;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ResEmployeeDTO {

    private UUID id;

    private String employeeCode;

    private String fullName;

    private String email;

    private String phone;

    private Gender gender;

    private String status;

    private UUID organizationId;

    private UUID positionId;

    private UUID managerId;

    private UUID shiftId;

    private String avatarUrl;

    private LocalDate hireDate;

    // ===== Lifecycle =====
    private LocalDate probationEndDate;
    private LocalDate confirmedDate;
    private LocalDate terminationDate;
    private String terminationReason;

    private Instant createdAt;
    private Instant updatedAt;

    private String createdBy;
    private String updatedBy;
}
