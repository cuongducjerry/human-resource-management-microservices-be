package com.hrm.attendance.dto.response;

import jakarta.persistence.Column;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

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
