package com.hrm.employee.dto.request;

import com.hrm.employee.util.constant.ContractStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqUpdateContractStatusDTO {

    @NotNull(message = "Status is required")
    private ContractStatus status;
}
