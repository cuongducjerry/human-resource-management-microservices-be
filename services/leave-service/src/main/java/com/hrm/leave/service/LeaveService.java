package com.hrm.leave.service;

import com.hrm.leave.client.EmployeeClient;
import com.hrm.leave.dto.request.ReqCreateLeaveRequestDTO;
import com.hrm.leave.dto.response.ResEmployeeDTO;
import com.hrm.leave.dto.response.ResLeaveBalanceDTO;
import com.hrm.leave.dto.response.ResLeaveRequestDTO;
import com.hrm.leave.dto.response.ResultPaginationDTO;
import com.hrm.leave.entity.LeaveBalance;
import com.hrm.leave.entity.LeaveRequest;
import com.hrm.leave.event.EmployeeCreatedEvent;
import com.hrm.leave.event.LeaveApprovedEvent;
import com.hrm.leave.event.LeaveCancelledEvent;
import com.hrm.leave.event.NotificationEvent;
import com.hrm.leave.mapper.LeaveMapper;
import com.hrm.leave.mapper.PaginationMapper;
import com.hrm.leave.repository.LeaveBalanceRepository;
import com.hrm.leave.repository.LeaveRequestRepository;
import com.hrm.leave.util.SecurityUtil;
import com.hrm.leave.util.constant.Gender;
import com.hrm.leave.util.constant.LeaveStatus;
import com.hrm.leave.util.constant.LeaveType;
import com.hrm.leave.util.constant.NotificationType;
import com.hrm.leave.util.error.BadRequestException;
import com.hrm.leave.util.error.ForbiddenException;
import com.hrm.leave.util.error.IdInvalidException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
    private final PaginationMapper paginationMapper;
    private final ApplicationEventPublisher eventPublisher;

    // ================= CREATE =================
    public ResLeaveRequestDTO create(ReqCreateLeaveRequestDTO req) {

        String id = SecurityUtil.getCurrentEmployeeId();
        UUID employeeId = UUID.fromString(id);

        validateEmployee(employeeId);
        validateDateRange(req.getStartDate(), req.getEndDate());
        validateOverlap(employeeId,
                req.getStartDate(),
                req.getEndDate());

        int totalDays = calculateTotalDays(
                req.getStartDate(),
                req.getEndDate());

        if (req.getLeaveType() != LeaveType.UNPAID) {
            LeaveBalance balance =
                    getLeaveBalance(employeeId, req.getLeaveType());

            validateSufficientBalance(balance, totalDays);
        }

        ResEmployeeDTO employee =
                employeeClient.getInternal(employeeId);

        LeaveRequest leave = LeaveRequest.builder()
                .employeeId(employeeId)
                .leaveType(req.getLeaveType())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .totalDays(totalDays)
                .status(LeaveStatus.PENDING)
                .reason(req.getReason())
                .escalated(false)
                .build();

        leaveRequestRepository.save(leave);

        // ===== SEND NOTIFICATION TO MANAGER =====
        if (employee.getManagerId() != null) {

            NotificationEvent event = NotificationEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .employeeId(employee.getManagerId().toString())
                    .email(employee.getEmail())
                    .title("Leave request approval")
                    .content(employee.getFullName()
                            + " submitted a leave request.")
                    .type(NotificationType.LEAVE)
                    .sendEmail(true)     ////////////////////////////////////////
                    .build();

            eventPublisher.publishEvent(event);
        }

        return leaveMapper.toDTO(leave);
    }

    // ================= APPROVE =================
    public void approve(UUID leaveId) {

        String approverId = SecurityUtil.getCurrentEmployeeId();

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
        leave.setManagerId(UUID.fromString(approverId));

        leaveRequestRepository.save(leave);

        LeaveApprovedEvent event = new LeaveApprovedEvent(
                        leave.getId(),
                        leave.getEmployeeId(),
                        leave.getLeaveType(),
                        leave.getStartDate(),
                        leave.getEndDate(),
                        leave.getTotalDays()
        );

        eventPublisher.publishEvent(event);

        // notification
        NotificationEvent notificationEvent = NotificationEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .employeeId(leave.getEmployeeId().toString())
                .type(NotificationType.LEAVE)
                .title("Leave Request Approved")
                .content("Your leave request has been approved successfully.")
                .sendEmail(false)
                .build();

        eventPublisher.publishEvent(notificationEvent);

    }

    // ================= REJECT =================
    public void reject(UUID leaveId) {

        LeaveRequest leave = getPendingLeave(leaveId);

        leave.setStatus(LeaveStatus.REJECTED);

        leaveRequestRepository.save(leave);

        NotificationEvent notification = NotificationEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .employeeId(leave.getEmployeeId().toString())
                .type(NotificationType.LEAVE)
                .title("Leave Request Rejected")
                .content("Your leave request has been rejected.")
                .sendEmail(false)
                .build();

        eventPublisher.publishEvent(notification);
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

        LeaveCancelledEvent event =
                new LeaveCancelledEvent(
                        leave.getId(),
                        leave.getEmployeeId(),
                        leave.getLeaveType(),
                        leave.getStartDate(),
                        leave.getEndDate(),
                        leave.getTotalDays()
        );

        eventPublisher.publishEvent(event);

        NotificationEvent notification = NotificationEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .employeeId(leave.getEmployeeId().toString())
                .type(NotificationType.LEAVE)
                .title("Leave Request Cancelled")
                .content("Your leave request has been cancelled successfully.")
                .sendEmail(true)
                .build();

        eventPublisher.publishEvent(notification);

    }

    // ================= LIST LEAVE REQUEST =================
    public ResultPaginationDTO handleListLeaveRequest(
            Specification<LeaveRequest> spec,
            Pageable pageable) {

        UUID currentEmployeeId =
                UUID.fromString(SecurityUtil.getCurrentEmployeeId());

        // ===== EMPLOYEE only views their own leave =====
        if (SecurityUtil.hasRole("ROLE_EMPLOYEE")) {

            Specification<LeaveRequest> employeeSpec =
                    (root, query, cb) ->
                            cb.equal(root.get("employeeId"), currentEmployeeId);

            spec = spec.and(employeeSpec);
        }

        // ===== The manager only watches the subordinates leave records =====
        if (SecurityUtil.hasRole("ROLE_MANAGER")) {

            Specification<LeaveRequest> managerSpec =
                    (root, query, cb) ->
                            cb.equal(root.get("managerId"), currentEmployeeId);

            spec = spec.and(managerSpec);
        }

        Page<LeaveRequest> page =
                leaveRequestRepository.findAll(spec, pageable);

        int pageNumber = pageable.getPageNumber() + 1;
        int pageSize = pageable.getPageSize();
        int totalPages = page.getTotalPages();
        long totalElements = page.getTotalElements();

        List<ResLeaveRequestDTO> list = page.getContent()
                .stream()
                .map(leaveMapper::toDTO)
                .toList();

        return paginationMapper.convertToResultPaginationDTO(
                pageNumber, pageSize, totalPages, totalElements, list);
    }

    public ResultPaginationDTO handleListLeaveRequestPesonal(
            Specification<LeaveRequest> spec,
            Pageable pageable) {

        UUID currentEmployeeId =
                UUID.fromString(SecurityUtil.getCurrentEmployeeId());

        // ===== EMPLOYEE only views their own leave =====
        if (SecurityUtil.hasRole("ROLE_EMPLOYEE") || SecurityUtil.hasRole("ROLE_MANAGER")) {

            Specification<LeaveRequest> employeeSpec =
                    (root, query, cb) ->
                            cb.equal(root.get("employeeId"), currentEmployeeId);

            spec = spec.and(employeeSpec);
        }

        // ===== The manager only watches the subordinates leave records =====
        Page<LeaveRequest> page = leaveRequestRepository.findAll(spec, pageable);

        int pageNumber = pageable.getPageNumber() + 1;
        int pageSize = pageable.getPageSize();
        int totalPages = page.getTotalPages();
        long totalElements = page.getTotalElements();

        List<ResLeaveRequestDTO> list = page.getContent()
                .stream()
                .map(leaveMapper::toDTO)
                .toList();

        return paginationMapper.convertToResultPaginationDTO(
                pageNumber, pageSize, totalPages, totalElements, list);
    }

    public List<ResLeaveBalanceDTO> getBalanceByEmployee(UUID employeeId) {

        UUID currentEmployeeId =
                UUID.fromString(SecurityUtil.getCurrentEmployeeId());

        // ===== EMPLOYEE =====
        if (SecurityUtil.hasRole("ROLE_EMPLOYEE")) {
            if (!employeeId.equals(currentEmployeeId)) {
                throw new ForbiddenException("You cannot view this leave balance");
            }
        }

        // ===== MANAGER =====
        if (SecurityUtil.hasRole("ROLE_MANAGER")) {

            List<UUID> subordinates =
                    employeeClient.getSubordinates(currentEmployeeId);

            if (!employeeId.equals(currentEmployeeId) && !subordinates.contains(employeeId)) {
                throw new ForbiddenException("You cannot view this employee leave balance");
            }
        }

        List<LeaveBalance> list =
                leaveBalanceRepository
                        .findByEmployeeIdAndYear(
                                employeeId,
                                Year.now().getValue()
                        );

        return list.stream()
                .map(balance -> {
                    ResLeaveBalanceDTO dto =
                            leaveMapper.toBalanceDTO(balance);

                    dto.setRemainingDays(
                            balance.getTotalDaysPerYear()
                                    - balance.getUsedDays());

                    return dto;
                })
                .toList();
    }



    public void initLeaveBalance(EmployeeCreatedEvent employee) {

        int year = Year.now().getValue();

        System.out.println("============================== EMPLOYEE ==============================");
        System.out.println(employee);
        System.out.println("=======================================================================");

        for (LeaveType type : LeaveType.values()) {

            if (type == LeaveType.UNPAID) {
                continue;
            }

            boolean exists = leaveBalanceRepository
                    .existsByEmployeeIdAndLeaveTypeAndYear(
                            employee.getId(), type, year
                    );

            if (exists) continue;

            Integer totalDays = resolveDefaultDays(type, employee);

            if (totalDays == null) continue;

            LeaveBalance balance = LeaveBalance.builder()
                    .employeeId(employee.getId())
                    .leaveType(type)
                    .totalDaysPerYear(totalDays)
                    .usedDays(0)
                    .year(year)
                    .build();

            leaveBalanceRepository.save(balance);
        }
    }

    private Integer resolveDefaultDays(LeaveType type,
                                       EmployeeCreatedEvent employee) {

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