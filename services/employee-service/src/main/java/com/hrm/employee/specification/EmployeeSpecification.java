package com.hrm.employee.specification;

import com.hrm.employee.entity.Employee;
import org.springframework.data.jpa.domain.Specification;

public class EmployeeSpecification {

    public static Specification<Employee> keyword(String keyword) {

        return (root, query, cb) -> {

            if (keyword == null || keyword.isEmpty()) {
                return cb.conjunction();
            }

            String like = "%" + keyword.toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("fullName")), like),
                    cb.like(cb.lower(root.get("email")), like),
                    cb.like(cb.lower(root.get("employeeCode")), like)
            );
        };
    }
}
