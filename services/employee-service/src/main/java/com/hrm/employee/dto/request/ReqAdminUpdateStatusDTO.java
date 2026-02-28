package com.hrm.employee.dto.request;

import com.hrm.employee.util.constant.EmployeeStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqAdminUpdateStatusDTO {

    @NotNull(message = "Status cannot be left blank")
    private EmployeeStatus status;
}