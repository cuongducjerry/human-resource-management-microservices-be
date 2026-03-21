package com.hrm.dashboard.repository;

import com.hrm.dashboard.entity.PositionStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PositionStatsRepository extends JpaRepository<PositionStats, UUID> {

    @Query("SELECT COUNT(p) FROM PositionStats p")
    Integer countAllPositions();

    Optional<PositionStats> findTopByOrderByCreatedAtDesc();
}