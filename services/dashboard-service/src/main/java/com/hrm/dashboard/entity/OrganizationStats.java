package com.hrm.dashboard.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "organization_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationStats {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "total_organization")
    private Integer totalOrganization;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}