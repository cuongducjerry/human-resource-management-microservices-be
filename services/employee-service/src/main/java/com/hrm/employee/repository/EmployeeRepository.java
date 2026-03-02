package com.hrm.employee.repository;


import com.hrm.employee.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID>, JpaSpecificationExecutor<Employee> {

    Optional<Employee> findByKeycloakUserId(String keycloakUserId);

    Optional<Employee> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<Employee> findByEmployeeCode(String employeeCode);

    @Query(value = "SELECT * FROM employees WHERE active = false",
            countQuery = "SELECT count(*) FROM employees WHERE active = false",
            nativeQuery = true)
    Page<Employee> findDeletedEmployees(Pageable pageable);

    @Query(value = "SELECT * FROM employees WHERE id = :id", nativeQuery = true)
    Optional<Employee> findByIdNative(@Param("id") UUID id);

    boolean existsByOrganizationId(UUID organizationId);

    boolean existsByPositionId(UUID positionId);
}
