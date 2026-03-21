package com.hrm.dashboard.controller;

import com.hrm.dashboard.entity.AttendanceEmployeeStats;
import com.hrm.dashboard.entity.LeaveEmployeeStats;
import com.hrm.dashboard.service.*;
import com.hrm.dashboard.util.SecurityUtil;
import com.hrm.dashboard.util.annotation.ApiMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final AttendanceStatsService attendanceStatsService;
    private final EmployeeStatsService employeeStatsService;
    private final LeaveStatsService leaveStatsService;
    private final OrganizationStatsService organizationStatsService;
    private final PositionStatsService positionStatsService;

    // ================= GET LATE STATS =================
    @GetMapping("/attendance/late")
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW')")
    @ApiMessage("Get late attendance stats for current user (HR_ADMIN, MANAGER, EMPLOYEE)")
    public ResponseEntity<Integer> getLateStatsForCurrentUser() {

        Integer stats = attendanceStatsService.getTotalLateForCurrentUser();
        return ResponseEntity.ok(stats);
    }

    // ================= GET TOP 8 LATE EMPLOYEES =================
    @GetMapping("/attendance/late/top8")
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW')")
    @ApiMessage("Get top 8 employees with most late attendance this month")
    public ResponseEntity<List<AttendanceEmployeeStats>> getTop8LateEmployees() {

        List<AttendanceEmployeeStats> stats = attendanceStatsService.getTop8LateEmployees();
        return ResponseEntity.ok(stats);
    }

    // ================= GET MY TOTAL LATE THIS MONTH =================
    @GetMapping("/attendance/late/my")
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW')")
    @ApiMessage("Get total late attendance for the current employee this month")
    public ResponseEntity<Integer> getMyTotalLateThisMonth() {
        int total = attendanceStatsService.getMyTotalLateThisMonth();
        return ResponseEntity.ok(total);
    }

    // ================= GET TOTAL LATE BY MONTH =================
    // ================= GET LATE STATS LAST 6 MONTHS =================
    @GetMapping("/attendance/late/6-months")
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW')")
    @ApiMessage("Get late attendance stats for last 6 months")
    public ResponseEntity<List<AttendanceEmployeeStats>> getLateStatsLast6Months() {

        List<AttendanceEmployeeStats> stats = attendanceStatsService.getLateStatsLast6Months();
        return ResponseEntity.ok(stats);
    }

    // ================= GET TOTAL EMPLOYEE =================
    @GetMapping("/employee/total")
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW')")
    @ApiMessage("Get total number of employees for current user role")
    public ResponseEntity<Integer> getTotalEmployeeForCurrentUser() {
        int total = employeeStatsService.getTotalEmployeeForCurrentUser();
        return ResponseEntity.ok(total);
    }

    // ================= GET LEAVE STATS =================
    @GetMapping("/leave/summary")
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW')")
    @ApiMessage("Get total leave stats for current user role this month")
    public ResponseEntity<Map<String, Object>> getLeaveStatsForCurrentUser() {

        return ResponseEntity.ok(
                leaveStatsService.getLeaveSummaryForCurrentUser()
        );
    }

    // ================= GET MY LEAVE STATS =================
    @GetMapping("/leave/my")
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW')")
    @ApiMessage("Get leave stats for the current employee this month")
    public ResponseEntity<Map<String, Integer>> getMyLeaveStatsThisMonth() {
        Map<String, Integer> stats = leaveStatsService.getMyLeaveStatsThisMonth();
        return ResponseEntity.ok(stats);
    }

    // ================= GET TOP 8 LEAVE EMPLOYEES =================
    @GetMapping("/leave/top8")
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW')")
    @ApiMessage("Get top 8 employees with most leave days this month")
    public ResponseEntity<List<LeaveEmployeeStats>> getTop8LeaveEmployees() {
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();
        List<LeaveEmployeeStats> stats = leaveStatsService.getTop8Employees(year, month);
        return ResponseEntity.ok(stats);
    }

    // ================= GET TOTAL ORGANIZATIONS =================
    @GetMapping("/organization/total")
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW')")
    @ApiMessage("Get total number of organizations")
    public ResponseEntity<Integer> getTotalOrganizations() {
        int total = organizationStatsService.getTotalOrganizations();
        return ResponseEntity.ok(total);
    }

    // ================= GET TOTAL POSITIONS =================
    @GetMapping("/position/total")
    @PreAuthorize("hasAuthority('DASHBOARD_VIEW')")
    @ApiMessage("Get total number of positions")
    public ResponseEntity<Integer> getTotalPositions() {
        int total = positionStatsService.getTotalPositions();
        return ResponseEntity.ok(total);
    }

}