package com.hrm.payroll.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "attendance_summary",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"employee_id", "month", "year"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID employeeId;

    private Integer month;

    private Integer year;

    private Integer workingDays;

    private Integer lateDays;

    private Integer absentDays;

    private Double totalWorkHours;

    private Double overtimeHours;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
