package com.hrm.dashboard.repository;

import com.hrm.dashboard.entity.OrganizationStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationStatsRepository extends JpaRepository<OrganizationStats, UUID> {

    @Query("SELECT COUNT(o) FROM OrganizationStats o")
    Integer countAllOrganizations();

    Optional<OrganizationStats> findTopByOrderByCreatedAtDesc();

}