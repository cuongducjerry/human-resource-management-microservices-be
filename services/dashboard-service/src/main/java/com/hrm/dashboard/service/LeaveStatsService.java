package com.hrm.dashboard.service;

import com.hrm.dashboard.dto.request.LeaveDashboardDTO;
import com.hrm.dashboard.entity.LeaveEmployeeStats;
import com.hrm.dashboard.repository.LeaveEmployeeStatsRepository;
import com.hrm.dashboard.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeaveStatsService {

    private final LeaveEmployeeStatsRepository repository;

    // ================= APPROVE =================
    @Transactional
    public void handleLeaveApproved(LeaveDashboardDTO dto) {

        int year = dto.getStartDate().getYear();
        int month = dto.getStartDate().getMonthValue();

        LeaveEmployeeStats stats = repository
                .findByEmployeeIdAndYearAndMonth(dto.getEmployeeId(), year, month)
                .orElseGet(() -> LeaveEmployeeStats.builder()
                        .employeeId(dto.getEmployeeId())
                        .organizationId(dto.getOrganizationId())
                        .positionId(dto.getPositionId())
                        .managerId(dto.getManagerId())
                        .year(year)
                        .month(month)
                        .totalLeaveRequests(0)
                        .totalLeaveDays(0)
                        .createdAt(LocalDateTime.now())
                        .build());

        stats.setTotalLeaveRequests(
                (stats.getTotalLeaveRequests() == null ? 0 : stats.getTotalLeaveRequests()) + 1
        );

        stats.setTotalLeaveDays(
                (stats.getTotalLeaveDays() == null ? 0 : stats.getTotalLeaveDays()) + dto.getTotalDays()
        );

        stats.setCreatedAt(LocalDateTime.now());
        repository.save(stats);
    }

    // ================= CANCEL =================
    @Transactional
    public void handleLeaveCancelled(LeaveDashboardDTO dto) {

        int year = dto.getStartDate().getYear();
        int month = dto.getStartDate().getMonthValue();

        repository.findByEmployeeIdAndYearAndMonth(dto.getEmployeeId(), year, month)
                .ifPresent(stats -> {
                    int newRequests = Math.max(0, (stats.getTotalLeaveRequests() == null ? 0 : stats.getTotalLeaveRequests()) - 1);
                    int newDays = Math.max(0, (stats.getTotalLeaveDays() == null ? 0 : stats.getTotalLeaveDays()) - dto.getTotalDays());

                    if (newRequests == 0 && newDays == 0) {
                        repository.delete(stats);
                        return;
                    }

                    stats.setTotalLeaveRequests(newRequests);
                    stats.setTotalLeaveDays(newDays);
                    stats.setCreatedAt(LocalDateTime.now());
                    repository.save(stats);
                });
    }

    // ================= QUERY =================
    public List<LeaveEmployeeStats> getTop8Employees(int year, int month) {

        List<String> roles = SecurityUtil.getCurrentUserRoles();
        UUID currentUserId = UUID.fromString(SecurityUtil.getCurrentEmployeeId());

        if (roles.contains("ROLE_HR_ADMIN") || roles.contains("ROLE_SUPER_ADMIN")) {
            return repository.findTopByMonth(year, month, PageRequest.of(0, 8));
        }

        List<LeaveEmployeeStats> stats = repository.findAllByMonth(year, month);

        if (roles.contains("ROLE_MANAGER")) {
            return stats.stream()
                    .filter(s ->
                            currentUserId.equals(s.getManagerId())
                                    || currentUserId.equals(s.getEmployeeId())
                    )
                    .sorted((a, b) -> Integer.compare(
                            b.getTotalLeaveDays() == null ? 0 : b.getTotalLeaveDays(),
                            a.getTotalLeaveDays() == null ? 0 : a.getTotalLeaveDays()
                    ))
                    .limit(8)
                    .toList();
        }

        return stats.stream()
                .filter(s -> currentUserId.equals(s.getEmployeeId()))
                .toList();
    }

    public Map<String, Object> getLeaveSummaryForCurrentUser() {

        List<String> roles = SecurityUtil.getCurrentUserRoles();
        UUID currentUserId = UUID.fromString(SecurityUtil.getCurrentEmployeeId());

        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();

        List<LeaveEmployeeStats> stats;

        if (roles.contains("ROLE_HR_ADMIN") || roles.contains("ROLE_SUPER_ADMIN")) {
            stats = repository.findAllByMonth(year, month);

        } else if (roles.contains("ROLE_MANAGER")) {
            stats = repository.findAllByMonth(year, month).stream()
                    .filter(s ->
                            currentUserId.equals(s.getManagerId())
                                    || currentUserId.equals(s.getEmployeeId())
                    )
                    .toList();

        } else {
            stats = repository.findAllByEmployeeIdAndYearAndMonth(currentUserId, year, month);
        }

        int totalRequests = stats.stream()
                .mapToInt(s -> s.getTotalLeaveRequests() == null ? 0 : s.getTotalLeaveRequests())
                .sum();

        int totalDays = stats.stream()
                .mapToInt(s -> s.getTotalLeaveDays() == null ? 0 : s.getTotalLeaveDays())
                .sum();

        return Map.of(
                "year", year,
                "month", month,
                "totalRequests", totalRequests,
                "totalDays", totalDays
        );
    }

    // ================= GET MY LEAVE STATS THIS MONTH =================
    @Transactional(readOnly = true)
    public Map<String, Integer> getMyLeaveStatsThisMonth() {
        UUID employeeId = UUID.fromString(SecurityUtil.getCurrentEmployeeId());
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();

        List<LeaveEmployeeStats> stats = repository.findAllByEmployeeIdAndYearAndMonth(employeeId, year, month);

        int totalRequests = stats.stream()
                .mapToInt(s -> s.getTotalLeaveRequests() == null ? 0 : s.getTotalLeaveRequests())
                .sum();

        int totalDays = stats.stream()
                .mapToInt(s -> s.getTotalLeaveDays() == null ? 0 : s.getTotalLeaveDays())
                .sum();

        return Map.of(
                "totalRequests", totalRequests,
                "totalDays", totalDays
        );
    }

}