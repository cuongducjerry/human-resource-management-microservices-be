package com.hrm.payroll.dto.response;

import com.hrm.payroll.util.constant.PayrollStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResPayrollDTO {

    private UUID id;

    private UUID employeeId;

    private Integer month;

    private Integer year;

    private BigDecimal baseSalary;

    private BigDecimal allowance;

    private BigDecimal overtimePay;

    private BigDecimal deduction;

    private BigDecimal netSalary;

    private PayrollStatus status;

    private Instant createdAt;

    private Instant updatedAt;

    private String createdBy;

    private String updatedBy;

}
