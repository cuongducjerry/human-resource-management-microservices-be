package com.hrm.dashboard.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "position_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PositionStats {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "total_position")
    private Integer totalPosition;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
