package com.hrm.dashboard.repository;

import com.hrm.dashboard.entity.AttendanceEmployeeStats;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceEmployeeStatsRepository extends JpaRepository<AttendanceEmployeeStats, UUID> {

    Optional<AttendanceEmployeeStats> findByEmployeeIdAndYearAndMonth(UUID employeeId, Integer year, Integer month);

    @Query("SELECT a FROM AttendanceEmployeeStats a WHERE a.year = :year AND a.month = :month ORDER BY a.totalLate DESC")
    List<AttendanceEmployeeStats> findTopLateEmployees(@Param("year") Integer year, @Param("month") Integer month, Pageable pageable);

    @Query("SELECT a FROM AttendanceEmployeeStats a WHERE a.year = :year AND a.month = :month")
    List<AttendanceEmployeeStats> findAllByMonth(@Param("year") Integer year, @Param("month") Integer month);
}
