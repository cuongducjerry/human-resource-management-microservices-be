package com.hrm.dashboard.service;

import com.hrm.dashboard.entity.OrganizationStats;
import com.hrm.dashboard.repository.OrganizationStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrganizationStatsService {

    private final OrganizationStatsRepository repository;

    @Transactional
    public OrganizationStats handleOrganizationCreated() {

        Optional<OrganizationStats> optionalStats = repository.findTopByOrderByCreatedAtDesc();

        OrganizationStats stats;
        if (optionalStats.isPresent()) {
            stats = optionalStats.get();
            stats.setTotalOrganization(stats.getTotalOrganization() + 1);
            stats.setCreatedAt(LocalDateTime.now());
        } else {
            stats = OrganizationStats.builder()
                    .totalOrganization(1)
                    .createdAt(LocalDateTime.now())
                    .build();
        }

        return repository.save(stats);
    }

    @Transactional
    public OrganizationStats handleOrganizationDeleted() {

        Optional<OrganizationStats> optionalStats = repository.findTopByOrderByCreatedAtDesc();

        if (optionalStats.isEmpty()) return null;

        OrganizationStats stats = optionalStats.get();

        int current = stats.getTotalOrganization() == null ? 0 : stats.getTotalOrganization();

        stats.setTotalOrganization(Math.max(0, current - 1));
        stats.setCreatedAt(LocalDateTime.now());

        return repository.save(stats);
    }

    // ===================== GET TOTAL =====================
    @Transactional(readOnly = true)
    public int getTotalOrganizations() {
        return repository.findTopByOrderByCreatedAtDesc()
                .map(OrganizationStats::getTotalOrganization)
                .orElse(0);
    }

}