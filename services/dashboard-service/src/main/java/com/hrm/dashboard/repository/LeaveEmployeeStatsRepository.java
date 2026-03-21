package com.hrm.dashboard.repository;

import com.hrm.dashboard.entity.LeaveEmployeeStats;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeaveEmployeeStatsRepository extends JpaRepository<LeaveEmployeeStats, UUID> {

    Optional<LeaveEmployeeStats> findByEmployeeIdAndYearAndMonth(UUID employeeId, Integer year, Integer month);

    @Query("SELECT l FROM LeaveEmployeeStats l WHERE l.year = :year AND l.month = :month ORDER BY l.totalLeaveDays DESC")
    List<LeaveEmployeeStats> findTopByMonth(@Param("year") Integer year, @Param("month") Integer month, Pageable pageable);

    @Query("SELECT l FROM LeaveEmployeeStats l WHERE l.year = :year AND l.month = :month")
    List<LeaveEmployeeStats> findAllByMonth(@Param("year") Integer year, @Param("month") Integer month);

    List<LeaveEmployeeStats> findAllByManagerIdAndYearAndMonth(UUID managerId, Integer year, Integer month);

    List<LeaveEmployeeStats> findAllByEmployeeIdAndYearAndMonth(UUID employeeId, Integer year, Integer month);
}
