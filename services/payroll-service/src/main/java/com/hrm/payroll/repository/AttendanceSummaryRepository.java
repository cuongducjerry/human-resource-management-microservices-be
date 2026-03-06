package com.hrm.payroll.repository;

import com.hrm.payroll.entity.AttendanceSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceSummaryRepository
        extends JpaRepository<AttendanceSummary, UUID>,
        JpaSpecificationExecutor<AttendanceSummary> {

    Optional<AttendanceSummary> findByEmployeeIdAndMonthAndYear(
            UUID employeeId,
            Integer month,
            Integer year
    );
}
