package com.hrm.payroll.dto.response;

import com.hrm.payroll.util.constant.ContractStatus;
import com.hrm.payroll.util.constant.ContractType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
public class ResContractDTO {

    private UUID id;
    private UUID employeeId;
    private ContractType type;
    private ContractStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal salary;
    private String description;
    private String fileUrl;

    private Instant createdAt;
    private Instant updatedAt;

    private String createdBy;
    private String updatedBy;
}