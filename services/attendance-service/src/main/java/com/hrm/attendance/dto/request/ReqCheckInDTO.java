package com.hrm.attendance.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ReqCheckInDTO {

    @NotNull
    private UUID employeeId;
}