package com.hrm.employee.dto.response;

import com.hrm.employee.util.constant.EmployeeStatus;
import com.hrm.employee.util.constant.Gender;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResEmployeeDTO {

    private UUID id;

    private String employeeCode;

    private String fullName;

    private String email;

    private String phone;

    private Gender gender;

    private EmployeeStatus status;

    private UUID departmentId;

    private UUID positionId;

    private LocalDate hireDate;

    private Instant createdAt;

    private Instant updatedAt;
}
