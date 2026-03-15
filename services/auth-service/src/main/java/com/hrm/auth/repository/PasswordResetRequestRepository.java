package com.hrm.auth.repository;

import com.hrm.auth.entity.PasswordResetRequest;
import com.hrm.auth.util.constant.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetRequestRepository extends JpaRepository<PasswordResetRequest, UUID>, JpaSpecificationExecutor<PasswordResetRequest> {

    List<PasswordResetRequest> findByStatus(Status status);

    Optional<PasswordResetRequest> findByKeycloakId(String keycloakId);

}
