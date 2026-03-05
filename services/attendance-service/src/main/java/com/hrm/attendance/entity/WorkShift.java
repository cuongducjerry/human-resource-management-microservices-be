package com.hrm.attendance.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "work_shifts")
@SQLDelete(sql = "UPDATE work_shifts SET active = false WHERE id = ?")
@Where(clause = "active = true")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkShift {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    private LocalTime startTime;

    private LocalTime endTime;

    private Integer allowLateMinutes;

    private Double standardWorkHours;


    // ===== Soft delete =====
    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    // ===== Auditing =====
    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @CreatedBy
    @Column(updatable = false)
    private String createdBy;

    @LastModifiedBy
    private String updatedBy;
}
