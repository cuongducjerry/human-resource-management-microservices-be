package com.hrm.leave.repository;

import com.hrm.leave.entity.LeaveRequest;
import com.hrm.leave.util.constant.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface LeaveRequestRepository
        extends JpaRepository<LeaveRequest, UUID>,
        JpaSpecificationExecutor<LeaveRequest> {

    @Query("""
        SELECT CASE WHEN COUNT(lr) > 0 THEN true ELSE false END
        FROM LeaveRequest lr
        WHERE lr.employeeId = :employeeId
          AND lr.status IN :statuses
          AND lr.startDate <= :endDate
          AND lr.endDate >= :startDate
    """)
    boolean existsOverlappingLeave(
            @Param("employeeId") UUID employeeId,
            @Param("statuses") List<LeaveStatus> statuses,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
