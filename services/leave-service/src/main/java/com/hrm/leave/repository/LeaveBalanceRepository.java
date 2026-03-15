package com.hrm.leave.repository;

import com.hrm.leave.entity.LeaveBalance;
import com.hrm.leave.util.constant.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeaveBalanceRepository
        extends JpaRepository<LeaveBalance, UUID> {

    Optional<LeaveBalance> findByEmployeeIdAndLeaveTypeAndYear(
            UUID employeeId,
            LeaveType leaveType,
            Integer year
    );

    boolean existsByEmployeeIdAndLeaveTypeAndYear(UUID employeeId, LeaveType type , int year);

    // ================= FIND BY EMPLOYEE + YEAR =================
    List<LeaveBalance> findByEmployeeIdAndYear(
            UUID employeeId,
            Integer year
    );

    @Modifying
    @Query("""
        UPDATE LeaveBalance lb
        SET lb.active = false
        WHERE lb.year = :year
    """)
    void deactivateByYear(int year);
}
