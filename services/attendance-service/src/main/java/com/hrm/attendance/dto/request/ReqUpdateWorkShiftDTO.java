package com.hrm.attendance.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class ReqUpdateWorkShiftDTO {

    @NotBlank(message = "Shift name must not be blank")
    private String name;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotNull(message = "Allow late minutes is required")
    @Min(value = 0, message = "Allow late minutes must be greater than or equal to 0")
    @Max(value = 180, message = "Allow late minutes must not exceed 180 minutes")
    private Integer allowLateMinutes;

    @NotNull(message = "Standard work hours is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Standard work hours must be greater than 0")
    @DecimalMax(value = "24.0", message = "Standard work hours must not exceed 24 hours")
    private Double standardWorkHours;
}
