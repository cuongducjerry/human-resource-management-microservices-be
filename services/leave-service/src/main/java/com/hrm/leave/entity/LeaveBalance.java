package com.hrm.leave.entity;

import com.hrm.leave.util.constant.LeaveType;
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
import java.util.UUID;

@Entity
@Table(name = "leave_balances",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"employeeId", "leaveType", "year"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE leave_balances SET active = false WHERE id = ?")
@Where(clause = "active = true")
@EntityListeners(AuditingEntityListener.class)
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID employeeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveType leaveType;

    @Column(nullable = false)
    private Integer totalDaysPerYear;

    @Builder.Default
    @Column(nullable = false)
    private Integer usedDays = 0;

    @Column(nullable = false)
    private Integer year;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

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

    // Do not save the database
    @Transient
    public Integer getRemainingDays() {
        return totalDaysPerYear - usedDays;
    }
}
