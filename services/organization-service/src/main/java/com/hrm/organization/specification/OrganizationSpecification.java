package com.hrm.organization.specification;

import com.hrm.organization.entity.Organization;
import com.hrm.organization.util.constant.OrganizationStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrganizationSpecification {

    public static Specification<Organization> filter(
            UUID id,
            OrganizationStatus status,
            String search
    ) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // Filter by id
            if (id != null) {
                predicates.add(
                        cb.equal(root.get("id"), id)
                );
            }

            // Filter by status
            if (status != null) {
                predicates.add(
                        cb.equal(root.get("status"), status)
                );
            }

            // Search by code OR name
            if (search != null && !search.trim().isEmpty()) {

                String keyword = "%" + search.trim().toLowerCase() + "%";

                Predicate codePredicate = cb.like(
                        cb.lower(root.get("code")),
                        keyword
                );

                Predicate namePredicate = cb.like(
                        cb.lower(root.get("name")),
                        keyword
                );

                predicates.add(
                        cb.or(codePredicate, namePredicate)
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}