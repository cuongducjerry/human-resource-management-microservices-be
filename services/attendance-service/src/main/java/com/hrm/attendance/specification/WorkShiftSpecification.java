package com.hrm.attendance.specification;

import com.hrm.attendance.entity.WorkShift;
import org.springframework.data.jpa.domain.Specification;

public class WorkShiftSpecification {

    public static Specification<WorkShift> hasName(String name) {
        return (root, query, cb) -> {

            if (name == null || name.trim().isEmpty()) {
                return null;
            }

            return cb.like(
                    cb.lower(root.get("name")),
                    "%" + name.toLowerCase() + "%"
            );
        };
    }
}