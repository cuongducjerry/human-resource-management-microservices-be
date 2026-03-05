package com.hrm.attendance.repository;

import com.hrm.attendance.entity.WorkShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

public interface WorkShiftRepository extends JpaRepository<WorkShift, UUID>, JpaSpecificationExecutor<WorkShift> {

    boolean existsByName(String name);

    boolean existsById(UUID id);
}