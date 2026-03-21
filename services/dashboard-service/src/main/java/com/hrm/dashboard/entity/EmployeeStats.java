package com.hrm.dashboard.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "employee_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeStats {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "total_employee")
    private Integer totalEmployee;

    @Column(name = "male")
    private Integer male;

    @Column(name = "female")
    private Integer female;

    @Column(name = "other_gender")
    private Integer otherGender;

    @Column(name = "organization_id")
    private UUID organizationId;

    @Column(name = "position_id")
    private UUID positionId;

    @Column(name = "manager_id")
    private UUID managerId;


}