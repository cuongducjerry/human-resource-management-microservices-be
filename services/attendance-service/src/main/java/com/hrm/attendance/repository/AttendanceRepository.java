package com.hrm.attendance.repository;

import com.hrm.attendance.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, UUID>, JpaSpecificationExecutor<Attendance> {

    Optional<Attendance> findByEmployeeIdAndWorkDate(UUID employeeId, LocalDate date);

    Page<Attendance> findByEmployeeId(
            UUID employeeId,
            Pageable pageable
    );

    Page<Attendance> findByWorkDateBetween(
            LocalDate start,
            LocalDate end,
            Pageable pageable
    );

    boolean existsByShiftId(UUID shiftId);
}
