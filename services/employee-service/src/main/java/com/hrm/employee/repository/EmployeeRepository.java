package com.hrm.employee.repository;


import com.hrm.employee.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
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

    boolean existsById(UUID id);

    @Query("SELECT e.id FROM Employee e WHERE e.active = true")
    List<UUID> findAllActiveEmployeeIds();

    @Query("SELECT e.id FROM Employee e WHERE e.managerId = :managerId AND e.active = true")
    List<UUID> findSubordinateIds(@Param("managerId") UUID managerId);

    @Query("""
        SELECT e.id
        FROM Employee e
        WHERE e.keycloakUserId IN :userIds
        AND e.active = true
    """)
    List<UUID> findIdsByKeycloakUserIds(@Param("userIds") List<String> userIds);

    @Query("""
       SELECT e FROM Employee e
       WHERE MONTH(e.dateOfBirth) = :month
       AND DAY(e.dateOfBirth) = :day
       """)
    List<Employee> findByMonthAndDay(int month, int day);
}
