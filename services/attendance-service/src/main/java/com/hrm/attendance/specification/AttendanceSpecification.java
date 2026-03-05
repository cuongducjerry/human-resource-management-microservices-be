package com.hrm.attendance.specification;

import com.hrm.attendance.entity.Attendance;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public class AttendanceSpecification {

    public static Specification<Attendance> byEmployee(UUID employeeId) {
        return (root, query, cb) ->
                employeeId == null ? null :
                        cb.equal(root.get("employeeId"), employeeId);
    }

    public static Specification<Attendance> byDateRange(
            LocalDate start,
            LocalDate end
    ) {
        return (root, query, cb) -> {

            if (start != null && end != null) {
                return cb.between(root.get("workDate"), start, end);
            }

            return null;
        };
    }
}
