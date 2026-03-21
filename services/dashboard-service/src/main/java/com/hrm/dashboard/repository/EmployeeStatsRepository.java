package com.hrm.dashboard.repository;

import com.hrm.dashboard.entity.EmployeeStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeStatsRepository extends JpaRepository<EmployeeStats, UUID> {

    Optional<EmployeeStats> findByOrganizationIdAndPositionIdAndManagerId(UUID organizationId, UUID positionId, UUID managerId);

    List<EmployeeStats> findAllByManagerId(UUID managerId);

    @Query("SELECT SUM(e.totalEmployee) FROM EmployeeStats e")
    Integer countAll();

    @Query("""
        SELECT SUM(e.totalEmployee)
        FROM EmployeeStats e
        WHERE e.managerId = :userId
           OR e.id = :userId
    """)
    Integer countByManagerIdIncludingSelf(@Param("userId") UUID userId);

}
