package com.hrm.organization.specification;

import com.hrm.organization.entity.Position;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PositionSpecification {

    public static Specification<Position> filter(
            UUID organizationId,
            String search
    ) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // Filter theo organizationId
            if (organizationId != null) {
                predicates.add(
                        cb.equal(root.get("organizationId"), organizationId)
                );
            }

            // Search theo name
            if (search != null && !search.trim().isEmpty()) {

                String keyword = "%" + search.trim().toLowerCase() + "%";

                predicates.add(
                        cb.like(
                                cb.lower(root.get("name")),
                                keyword
                        )
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
