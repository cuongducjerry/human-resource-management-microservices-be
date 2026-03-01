package com.hrm.employee.repository;

import com.hrm.employee.entity.Contract;
import com.hrm.employee.util.constant.ContractStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContractRepository extends
        JpaRepository<Contract, UUID>,
        JpaSpecificationExecutor<Contract> {

    Optional<Contract> findById(UUID id);

    boolean existsByEmployeeIdAndStatus(UUID employeeId, ContractStatus status);

    @Modifying
    @Query("""
        UPDATE Contract c
           SET c.status = :expiredStatus
         WHERE c.status = :activeStatus
           AND c.endDate < :today
    """)
    int bulkExpire(
            ContractStatus activeStatus,
            ContractStatus expiredStatus,
            LocalDate today
    );

}
