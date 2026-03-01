package com.hrm.employee.specification;

import com.hrm.employee.entity.Contract;
import com.hrm.employee.util.constant.ContractStatus;
import com.hrm.employee.util.constant.ContractType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ContractSpecification {

    public static Specification<Contract> filter(
            UUID id,
            ContractStatus status,
            ContractType type
    ) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (id != null) {
                predicates.add(cb.equal(root.get("id"), id));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}