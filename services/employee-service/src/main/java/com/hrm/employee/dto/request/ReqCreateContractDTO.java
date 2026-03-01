package com.hrm.employee.dto.request;

import com.hrm.employee.util.constant.ContractType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class ReqCreateContractDTO {

    @NotNull(message = "Employee ID is required")
    private UUID employeeId;

    @NotNull(message = "Contract type is required")
    private ContractType type;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;

    @NotNull(message = "Salary is required")
    @Positive(message = "Salary must be greater than 0")
    private BigDecimal salary;

    private String description;
}