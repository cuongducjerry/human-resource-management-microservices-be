package com.hrm.organization.repository;

import com.hrm.organization.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PositionRepository extends JpaRepository<Position, UUID>, JpaSpecificationExecutor<Position> {

    boolean existsByOrganizationId(UUID organizationId);

    List<Position> findByOrganizationId(UUID organizationId);

    boolean existsByNameAndOrganizationIdAndIdNot(
            String name,
            UUID organizationId,
            UUID id
    );
}
