package com.hrm.employee.dto.request;

import com.hrm.employee.util.constant.ContractType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ReqUpdateContractDTO {

    @NotNull(message = "Contract type is required")
    private ContractType type;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;

    @NotNull(message = "Salary is required")
    private BigDecimal salary;

    private String description;
}
