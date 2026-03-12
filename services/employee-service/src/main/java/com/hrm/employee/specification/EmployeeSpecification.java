package com.hrm.employee.specification;

import com.hrm.employee.entity.Employee;
import com.hrm.employee.util.constant.EmployeeStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class EmployeeSpecification {

    public static Specification<Employee> filter(String keyword, EmployeeStatus status) {

        return (root, query, cb) -> {

            Predicate predicate = cb.conjunction();

            // ===== KEYWORD SEARCH =====
            if (keyword != null && !keyword.isEmpty()) {

                String like = "%" + keyword.toLowerCase() + "%";

                Predicate keywordPredicate = cb.or(
                        cb.like(cb.lower(root.get("fullName")), like),
                        cb.like(cb.lower(root.get("email")), like),
                        cb.like(cb.lower(root.get("employeeCode")), like)
                );

                predicate = cb.and(predicate, keywordPredicate);
            }

            // ===== STATUS FILTER =====
            if (status != null) {

                Predicate statusPredicate = cb.equal(root.get("status"), status);

                predicate = cb.and(predicate, statusPredicate);
            }

            return predicate;
        };
    }
}
