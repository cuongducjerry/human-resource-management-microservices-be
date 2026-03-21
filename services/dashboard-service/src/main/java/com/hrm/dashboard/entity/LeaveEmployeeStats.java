package com.hrm.dashboard.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "leave_employee_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveEmployeeStats {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "position_id", nullable = false)
    private UUID positionId;

    @Column(name = "manager_id", nullable = false)
    private UUID managerId;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "month", nullable = false)
    private Integer month;

    @Column(name = "total_leave_requests")
    private Integer totalLeaveRequests;

    @Column(name = "total_leave_days")
    private Integer totalLeaveDays;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}