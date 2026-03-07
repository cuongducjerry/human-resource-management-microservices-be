package com.hrm.payroll.specification;

import com.hrm.payroll.entity.LeaveSummary;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LeaveSummarySpecification {

    public static Specification<LeaveSummary> filter(
            UUID employeeId,
            Integer month,
            Integer year
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

            predicates.add(cb.isTrue(root.get("active")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
