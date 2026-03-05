package com.hrm.employee.dto.response;

import lombok.*;

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResWorkShiftDTO {

    private UUID id;

    private String name;

    private LocalTime startTime;

    private LocalTime endTime;

    private Integer allowLateMinutes;

    private Double standardWorkHours;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

}