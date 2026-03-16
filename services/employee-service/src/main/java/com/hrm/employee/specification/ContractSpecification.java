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
            UUID employeeId,
            ContractStatus status,
            ContractType type
    ) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (employeeId != null) {
                predicates.add(cb.equal(root.get("employeeId"), employeeId));
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

    // ===== ADMIN / HR =====
    public static Specification<Contract> filterForAdmin(
            UUID contractId,
            ContractStatus status,
            ContractType type
    ) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (contractId != null) {
                predicates.add(cb.equal(root.get("id"), contractId));
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

    // ===== MANAGER =====
    public static Specification<Contract> filterForManager(
            UUID contractId,
            List<UUID> employeeIds,
            ContractStatus status,
            ContractType type
    ) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // manager chỉ xem contract của team
            if (employeeIds != null && !employeeIds.isEmpty()) {
                predicates.add(root.get("employeeId").in(employeeIds));
            }

            // filter contract id
            if (contractId != null) {
                predicates.add(cb.equal(root.get("id"), contractId));
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