package com.hrm.employee.dto.request;

import com.hrm.employee.util.constant.Gender;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class ReqUpdateEmployeeDTO {

    private String fullName;
    private String email;
    private String phone;
    private Gender gender;

    private UUID organizationId;
    private UUID positionId;
    private UUID managerId;
    private UUID shiftId;

    private LocalDate hireDate;
    private LocalDate probationEndDate;
    private LocalDate confirmedDate;

    private LocalDate terminationDate;
    private String terminationReason;
}
