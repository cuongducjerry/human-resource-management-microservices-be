package com.hrm.leave.specification;

import com.hrm.leave.entity.LeaveRequest;
import com.hrm.leave.util.constant.LeaveStatus;
import com.hrm.leave.util.constant.LeaveType;
import org.springframework.data.jpa.domain.Specification;

public class LeaveSpecification {

    public static Specification<LeaveRequest> filter(
            String keyword,
            LeaveStatus status,
            LeaveType type
    ) {

        return (root, query, cb) -> {

            var predicates = cb.conjunction();

            // ===== Keyword (search reason) =====
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.toLowerCase() + "%";
                predicates = cb.and(predicates,
                        cb.like(cb.lower(root.get("reason")), like));
            }

            // ===== Filter by Status =====
            if (status != null) {
                predicates = cb.and(predicates,
                        cb.equal(root.get("status"), status));
            }

            // ===== Filter by LeaveType =====
            if (type != null) {
                predicates = cb.and(predicates,
                        cb.equal(root.get("leaveType"), type));
            }

            return predicates;
        };
    }
}