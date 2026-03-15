package com.hrm.auth.specification;

import com.hrm.auth.entity.PasswordResetRequest;
import com.hrm.auth.util.constant.Status;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class PasswordResetRequestSpecification {

    public static Specification<PasswordResetRequest> filter(
            String keyword,
            Status status
    ) {

        return (root, query, cb) -> {

            Predicate predicate = cb.conjunction();

            if (keyword != null && !keyword.isEmpty()) {

                Predicate emailLike =
                        cb.like(
                                cb.lower(root.get("email")),
                                "%" + keyword.toLowerCase() + "%"
                        );

                predicate = cb.and(predicate, emailLike);
            }

            if (status != null) {

                Predicate statusEqual =
                        cb.equal(root.get("status"), status);

                predicate = cb.and(predicate, statusEqual);
            }

            return predicate;
        };
    }
}
