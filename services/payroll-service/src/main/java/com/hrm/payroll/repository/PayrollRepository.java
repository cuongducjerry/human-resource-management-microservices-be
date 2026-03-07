package com.hrm.payroll.repository;

import com.hrm.payroll.entity.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, UUID>, JpaSpecificationExecutor<Payroll> {

    List<Payroll> findByMonthAndYear(Integer month, Integer year);

    Optional<Payroll> findByEmployeeIdAndMonthAndYear(
            UUID employeeId,
            Integer month,
            Integer year
    );

}