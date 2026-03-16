package com.hrm.payroll.specification;

import com.hrm.payroll.entity.Payroll;
import com.hrm.payroll.util.constant.PayrollStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PayrollSpecification {

    public static Specification<Payroll> filterByEmployees(
            List<UUID> employeeIds,
            Integer month,
            Integer year,
            PayrollStatus status
    ) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (employeeIds != null && !employeeIds.isEmpty()) {
                predicates.add(root.get("employeeId").in(employeeIds));
            }

            if (month != null) {
                predicates.add(cb.equal(root.get("month"), month));
            }

            if (year != null) {
                predicates.add(cb.equal(root.get("year"), year));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Payroll> filter(
            UUID employeeId,
            Integer month,
            Integer year,
            PayrollStatus status
    ) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (employeeId != null) {
                predicates.add(
                        cb.equal(root.get("employeeId"), employeeId)
                );
            }

            if (month != null) {
                predicates.add(
                        cb.equal(root.get("month"), month)
                );
            }

            if (year != null) {
                predicates.add(
                        cb.equal(root.get("year"), year)
                );
            }

            if (status != null) {
                predicates.add(
                        cb.equal(root.get("status"), status)
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

}
