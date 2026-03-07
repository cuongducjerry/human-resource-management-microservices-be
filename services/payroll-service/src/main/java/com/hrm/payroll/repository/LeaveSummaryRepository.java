package com.hrm.payroll.repository;

import com.hrm.payroll.entity.LeaveSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeaveSummaryRepository
        extends JpaRepository<LeaveSummary, UUID>,
        JpaSpecificationExecutor<LeaveSummary> {

    Optional<LeaveSummary> findByEmployeeIdAndMonthAndYear(
            UUID employeeId,
            Integer month,
            Integer year
    );
}
