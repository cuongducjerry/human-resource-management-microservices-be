package com.hrm.dashboard.service;

import com.hrm.dashboard.dto.request.EmployeeDashboardDTO;
import com.hrm.dashboard.entity.EmployeeStats;
import com.hrm.dashboard.repository.EmployeeStatsRepository;
import com.hrm.dashboard.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeStatsService {

    private final EmployeeStatsRepository repo;

    @Transactional
    public void handleEmployeeCreated(EmployeeDashboardDTO data) {

        EmployeeStats stats = repo.findByOrganizationIdAndPositionIdAndManagerId(
                data.getOrganizationId(),
                data.getPositionId(),
                data.getManagerId()
        ).orElseGet(() -> EmployeeStats.builder()
                .organizationId(data.getOrganizationId())
                .positionId(data.getPositionId())
                .managerId(data.getManagerId())
                .totalEmployee(0)
                .male(0)
                .female(0)
                .otherGender(0)
                .build()
        );

        stats.setTotalEmployee((stats.getTotalEmployee() == null ? 0 : stats.getTotalEmployee()) + 1);

        if ("MALE".equalsIgnoreCase(data.getGender())) {
            stats.setMale((stats.getMale() == null ? 0 : stats.getMale()) + 1);
        } else if ("FEMALE".equalsIgnoreCase(data.getGender())) {
            stats.setFemale((stats.getFemale() == null ? 0 : stats.getFemale()) + 1);
        } else {
            stats.setOtherGender((stats.getOtherGender() == null ? 0 : stats.getOtherGender()) + 1);
        }

        repo.save(stats);
    }

    public List<EmployeeStats> getStatsForCurrentUser() {
        List<String> roles = SecurityUtil.getCurrentUserRoles();


        if (roles.contains("ROLE_HR_ADMIN")) {
            return repo.findAll();
        }


        if (roles.contains("ROLE_MANAGER")) {
            UUID managerId = UUID.fromString(SecurityUtil.getCurrentEmployeeId());
            return repo.findAllByManagerId(managerId);
        }

        return Collections.emptyList();
    }

    public int getTotalEmployeeForCurrentUser() {
        List<String> roles = SecurityUtil.getCurrentUserRoles();

        if (roles.contains("ROLE_HR_ADMIN")) {
            return Optional.ofNullable(repo.countAll()).orElse(0);
        }

        if (roles.contains("ROLE_MANAGER")) {
            UUID userId = UUID.fromString(SecurityUtil.getCurrentEmployeeId());
            return Optional.ofNullable(repo.countByManagerIdIncludingSelf(userId)).orElse(0);
        }

        return 0;
    }
}
