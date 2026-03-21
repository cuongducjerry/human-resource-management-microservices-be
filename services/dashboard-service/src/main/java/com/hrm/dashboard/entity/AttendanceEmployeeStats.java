package com.hrm.dashboard.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "attendance_employee_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceEmployeeStats {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "organization_id")
    private UUID organizationId;

    @Column(name = "position_id")
    private UUID positionId;

    @Column(name = "manager_id")
    private UUID managerId;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "month", nullable = false)
    private Integer month;

    @Column(name = "total_late")
    private Integer totalLate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}