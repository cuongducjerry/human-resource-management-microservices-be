package com.hrm.leave.service;

import com.hrm.leave.client.EmployeeClient;
import com.hrm.leave.dto.request.ReqCreateLeaveRequestDTO;
import com.hrm.leave.dto.response.ResEmployeeDTO;
import com.hrm.leave.dto.response.ResLeaveRequestDTO;
import com.hrm.leave.entity.LeaveBalance;
import com.hrm.leave.entity.LeaveRequest;
import com.hrm.leave.mapper.LeaveMapper;
import com.hrm.leave.repository.LeaveBalanceRepository;
import com.hrm.leave.repository.LeaveRequestRepository;
import com.hrm.leave.util.SecurityUtil;
import com.hrm.leave.util.constant.Gender;
import com.hrm.leave.util.constant.LeaveStatus;
import com.hrm.leave.util.constant.LeaveType;
import com.hrm.leave.util.error.BadRequestException;
import com.hrm.leave.util.error.IdInvalidException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final EmployeeClient employeeClient;
    private final LeaveMapper leaveMapper;

    // ================= CREATE =================
    public ResLeaveRequestDTO create(ReqCreateLeaveRequestDTO req) {

        validateEmployee(req.getEmployeeId());
        validateDateRange(req.getStartDate(), req.getEndDate());
        validateOverlap(req.getEmployeeId(),
                req.getStartDate(),
                req.getEndDate());

        int totalDays = calculateTotalDays(
                req.getStartDate(),
                req.getEndDate());

        if (req.getLeaveType() != LeaveType.UNPAID) {
            LeaveBalance balance =
                    getLeaveBalance(req.getEmployeeId(), req.getLeaveType());

            validateSufficientBalance(balance, totalDays);
        }

        LeaveRequest leave = LeaveRequest.builder()
                .employeeId(req.getEmployeeId())
                .leaveType(req.getLeaveType())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .totalDays(totalDays)
                .status(LeaveStatus.PENDING)
                .reason(req.getReason())
                .build();

        leaveRequestRepository.save(leave);

        return leaveMapper.toDTO(leave);
    }

    // ================= APPROVE =================
    public void approve(UUID leaveId) {

        String approverId = SecurityUtil.getCurrentUserId();

        LeaveRequest leave = getPendingLeave(leaveId);

        if (leave.getLeaveType() != LeaveType.UNPAID) {

            LeaveBalance balance =
                    getLeaveBalance(leave.getEmployeeId(), leave.getLeaveType());

            validateSufficientBalance(balance, leave.getTotalDays());

            balance.setUsedDays(
                    balance.getUsedDays() + leave.getTotalDays());

            leaveBalanceRepository.save(balance);
        }

        leave.setStatus(LeaveStatus.APPROVED);
        leave.setApproverId(approverId);

        leaveRequestRepository.save(leave);
    }

    // ================= REJECT =================
    public void reject(UUID leaveId) {

        LeaveRequest leave = getPendingLeave(leaveId);

        leave.setStatus(LeaveStatus.REJECTED);

        leaveRequestRepository.save(leave);
    }

    // ================= CANCEL =================
    public void cancel(UUID leaveId) {

        LeaveRequest leave = findLeaveById(leaveId);

        if (leave.getStatus() == LeaveStatus.CANCELLED
                || leave.getStatus() == LeaveStatus.REJECTED) {
            throw new BadRequestException("Cannot cancel this leave");
        }

        LocalDate today = LocalDate.now();

        if (!leave.getStartDate().isAfter(today)) {
            throw new BadRequestException(
                    "Cannot cancel leave that already started");
        }

        if (leave.getStatus() == LeaveStatus.APPROVED
                && leave.getLeaveType() != LeaveType.UNPAID) {

            LeaveBalance balance =
                    getLeaveBalance(leave.getEmployeeId(),
                            leave.getLeaveType());

            balance.setUsedDays(
                    balance.getUsedDays() - leave.getTotalDays());

            leaveBalanceRepository.save(balance);
        }

        leave.setStatus(LeaveStatus.CANCELLED);

        leaveRequestRepository.save(leave);
    }

    public void initLeaveBalance(UUID employeeId) {

        int year = Year.now().getValue();

        ResEmployeeDTO employee =
                employeeClient.getInternal(employeeId);

        for (LeaveType type : LeaveType.values()) {

            if (type == LeaveType.UNPAID) {
                continue;
            }

            boolean exists = leaveBalanceRepository
                    .existsByEmployeeIdAndLeaveTypeAndYear(
                            employeeId, type, year
                    );

            if (exists) continue;

            Integer totalDays = resolveDefaultDays(type, employee);

            if (totalDays == null) continue;

            LeaveBalance balance = LeaveBalance.builder()
                    .employeeId(employeeId)
                    .leaveType(type)
                    .totalDaysPerYear(totalDays)
                    .usedDays(0)
                    .year(year)
                    .build();

            leaveBalanceRepository.save(balance);
        }
    }

    private Integer resolveDefaultDays(LeaveType type,
                                       ResEmployeeDTO employee) {

        switch (type) {

            case ANNUAL:
                return 12;

            case SICK:
                return 10;

            case MATERNITY:
                return employee.getGender() == Gender.FEMALE ? 180 : null;

            case PATERNITY:
                return employee.getGender() == Gender.MALE ? 10 : null;

            default:
                return null;
        }
    }

    // ================= PRIVATE METHODS =================

    private void validateEmployee(UUID employeeId) {
        if (!employeeClient.existsById(employeeId)) {
            throw new IdInvalidException("Employee not found");
        }
    }

    private void validateDateRange(LocalDate start, LocalDate end) {
        if (end.isBefore(start)) {
            throw new BadRequestException("End date must be after start date");
        }
    }

    private void validateOverlap(UUID employeeId,
                                 LocalDate start,
                                 LocalDate end) {

        boolean overlap = leaveRequestRepository.existsOverlappingLeave(
                employeeId,
                List.of(LeaveStatus.PENDING, LeaveStatus.APPROVED),
                start,
                end
        );

        if (overlap) {
            throw new BadRequestException("Leave date overlaps");
        }
    }

    private int calculateTotalDays(LocalDate start, LocalDate end) {
        return (int) ChronoUnit.DAYS.between(start, end) + 1;
    }

    private LeaveBalance getLeaveBalance(UUID employeeId,
                                         LeaveType type) {

        return leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeAndYear(
                        employeeId,
                        type,
                        Year.now().getValue()
                )
                .orElseThrow(() ->
                        new BadRequestException("Leave balance not found"));
    }

    private void validateSufficientBalance(LeaveBalance balance,
                                           int totalDays) {

        if (balance.getRemainingDays() < totalDays) {
            throw new BadRequestException("Not enough leave balance");
        }
    }

    private LeaveRequest findLeaveById(UUID id) {
        return leaveRequestRepository.findById(id)
                .orElseThrow(() ->
                        new IdInvalidException("Leave not found"));
    }

    private LeaveRequest getPendingLeave(UUID id) {
        LeaveRequest leave = findLeaveById(id);

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Leave is not pending");
        }

        return leave;
    }
}