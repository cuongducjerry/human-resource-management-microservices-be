package com.hrm.employee.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ReqUpdateEmployeeRolesDTO {
    @NotEmpty
    private List<String> roles;
}
