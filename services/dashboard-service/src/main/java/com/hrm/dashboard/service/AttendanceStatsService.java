package com.hrm.dashboard.service;

import com.hrm.dashboard.dto.request.AttendanceDashboardDTO;
import com.hrm.dashboard.entity.AttendanceEmployeeStats;
import com.hrm.dashboard.repository.AttendanceEmployeeStatsRepository;
import com.hrm.dashboard.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttendanceStatsService {

    private final AttendanceEmployeeStatsRepository repository;

    @Transactional
    public void handleEmployeeLate(AttendanceDashboardDTO dto) {
        if (!dto.isLate()) return;

        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();

        AttendanceEmployeeStats stats = repository
                .findByEmployeeIdAndYearAndMonth(dto.getEmployeeId(), year, month)
                .orElseGet(() -> AttendanceEmployeeStats.builder()
                        .employeeId(dto.getEmployeeId())
                        .organizationId(dto.getOrganizationId())
                        .positionId(dto.getPositionId())
                        .managerId(dto.getManagerId())
                        .year(year)
                        .month(month)
                        .totalLate(0)
                        .createdAt(LocalDateTime.now())
                        .build());

        stats.setTotalLate((stats.getTotalLate() == null ? 0 : stats.getTotalLate()) + 1);
        stats.setCreatedAt(LocalDateTime.now());

        repository.save(stats);
    }


    // ================= GET LAST 6 MONTHS LATE STATS =================
    public List<AttendanceEmployeeStats> getLateStatsLast6Months() {

        List<String> roles = SecurityUtil.getCurrentUserRoles();
        UUID currentUserId = UUID.fromString(SecurityUtil.getCurrentEmployeeId());

        List<AttendanceEmployeeStats> result = new ArrayList<>();
        LocalDate now = LocalDate.now();

        for (int i = 5; i >= 0; i--) {
            LocalDate date = now.minusMonths(i);
            int year = date.getYear();
            int month = date.getMonthValue();

            List<AttendanceEmployeeStats> stats;

            if (roles.contains("ROLE_HR_ADMIN")) {
                stats = repository.findAllByMonth(year, month);
            } else if (roles.contains("ROLE_MANAGER")) {
                stats = repository.findAllByMonth(year, month).stream()
                        .filter(a ->
                                currentUserId.equals(a.getManagerId())
                                        || currentUserId.equals(a.getEmployeeId())
                        )
                        .toList();
            } else {
                stats = repository.findAllByMonth(year, month).stream()
                        .filter(a -> currentUserId.equals(a.getEmployeeId()))
                        .toList();
            }

            result.addAll(stats);
        }

        return result;
    }

    public Integer getTotalLateForCurrentUser() {
        List<String> roles = SecurityUtil.getCurrentUserRoles();

        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();

        List<AttendanceEmployeeStats> stats;

        if (roles.contains("ROLE_HR_ADMIN")) {
            stats = repository.findAllByMonth(year, month);
        } else if (roles.contains("ROLE_MANAGER")) {
            UUID managerId = UUID.fromString(SecurityUtil.getCurrentEmployeeId());
            stats = repository.findAllByMonth(year, month).stream()
                    .filter(a ->
                            managerId.equals(a.getManagerId())
                                    || managerId.equals(a.getEmployeeId())
                    )
                    .toList();
        } else {
            UUID employeeId = UUID.fromString(SecurityUtil.getCurrentEmployeeId());
            stats = repository.findAllByMonth(year, month).stream()
                    .filter(a -> employeeId.equals(a.getEmployeeId()))
                    .toList();
        }

        return stats.stream()
                .mapToInt(a -> a.getTotalLate() == null ? 0 : a.getTotalLate())
                .sum();
    }

    public List<AttendanceEmployeeStats> getTop8LateEmployees() {

        List<String> roles = SecurityUtil.getCurrentUserRoles();
        UUID currentUserId = UUID.fromString(SecurityUtil.getCurrentEmployeeId());

        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();

        List<AttendanceEmployeeStats> stats;

        if (roles.contains("ROLE_HR_ADMIN")) {
            return repository.findTopLateEmployees(year, month, PageRequest.of(0, 8));
        }

        stats = repository.findAllByMonth(year, month);

        if (roles.contains("ROLE_MANAGER")) {
            stats = stats.stream()
                    .filter(a ->
                            currentUserId.equals(a.getManagerId())
                                    || currentUserId.equals(a.getEmployeeId())
                    )
                    .sorted((a, b) -> Integer.compare(
                            b.getTotalLate() == null ? 0 : b.getTotalLate(),
                            a.getTotalLate() == null ? 0 : a.getTotalLate()
                    ))
                    .limit(8)
                    .toList();
        } else {
            stats = stats.stream()
                    .filter(a -> currentUserId.equals(a.getEmployeeId()))
                    .toList();
        }

        return stats;
    }

    public int getMyTotalLateThisMonth() {
        UUID employeeId = UUID.fromString(SecurityUtil.getCurrentEmployeeId());
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();

        return repository.findAllByMonth(year, month).stream()
                .filter(a -> employeeId.equals(a.getEmployeeId()))
                .mapToInt(a -> a.getTotalLate() == null ? 0 : a.getTotalLate())
                .sum();
    }

}