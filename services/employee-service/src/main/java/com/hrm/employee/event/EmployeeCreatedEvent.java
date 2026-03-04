package com.hrm.employee.event;

import com.hrm.employee.util.constant.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeCreatedEvent {
    private UUID id;
    private String employeeCode;
    private String fullName;
    private UUID organizationId;
    private Gender gender;
}