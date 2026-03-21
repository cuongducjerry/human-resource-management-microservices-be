package com.hrm.attendance.service;

import com.hrm.attendance.client.EmployeeClient;
import com.hrm.attendance.dto.request.AttendanceDashboardDTO;
import com.hrm.attendance.dto.response.ResAttendanceDTO;
import com.hrm.attendance.dto.response.ResEmployeeDTO;
import com.hrm.attendance.dto.response.ResultPaginationDTO;
import com.hrm.attendance.entity.Attendance;
import com.hrm.attendance.entity.WorkShift;
import com.hrm.attendance.event.AttendanceRecordedEvent;
import com.hrm.attendance.event.DashboardEvent;
import com.hrm.attendance.mapper.AttendanceMapper;
import com.hrm.attendance.mapper.PaginationMapper;
import com.hrm.attendance.repository.AttendanceRepository;
import com.hrm.attendance.specification.AttendanceSpecification;
import com.hrm.attendance.util.SecurityUtil;
import com.hrm.attendance.util.constant.AttendanceStatus;
import com.hrm.attendance.util.error.BadRequestException;
import com.hrm.attendance.util.error.ForbiddenException;
import com.hrm.attendance.util.error.IdInvalidException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final AttendanceMapper attendanceMapper;
    private final PaginationMapper paginationMapper;
    private final WorkShiftService workShiftService;
    private final EmployeeClient employeeClient;
    private final ApplicationEventPublisher eventPublisher;

    // ===== CHECK IN =====
    @Transactional
    public ResAttendanceDTO checkIn() {

        String employeeIdStr = SecurityUtil.getCurrentEmployeeId();

        if (employeeIdStr == null) {
            throw new BadRequestException("EmployeeId not found in token");
        }

        UUID employeeId = UUID.fromString(employeeIdStr);

        LocalDate today = LocalDate.now();

        if (attendanceRepository
                .findByEmployeeIdAndWorkDate(employeeId, today)
                .isPresent()) {
            throw new BadRequestException("Already checked in today");
        }

        UUID shiftId = employeeClient.getInternal(employeeId).getShiftId();

        WorkShift shift = workShiftService.getEntityById(shiftId);

        LocalDateTime now = LocalDateTime.now();
        LocalTime checkInTime = now.toLocalTime();

        boolean isLate = checkInTime.isAfter(
                shift.getStartTime()
                        .plusMinutes(shift.getAllowLateMinutes())
        );

        AttendanceStatus status = AttendanceStatus.PRESENT;

        if (isLate) {
            status = AttendanceStatus.LATE;
        }

        Attendance attendance = Attendance.builder()
                .employeeId(employeeId)
                .shiftId(shiftId)
                .workDate(today)
                .checkInTime(now)
                .status(status)
                .late(isLate)
                .earlyLeave(false)
                .build();

        attendanceRepository.save(attendance);

        if (isLate) {
            ResEmployeeDTO employeeDto = employeeClient.getInternal(employeeId);

            DashboardEvent dashboardEvent = DashboardEvent.builder()
                    .type("ATTENDANCE_LATE")
                    .data(AttendanceDashboardDTO.builder()
                            .employeeId(employeeId)
                            .organizationId(employeeDto.getOrganizationId())
                            .positionId(employeeDto.getPositionId())
                            .managerId(employeeDto.getManagerId())
                            .late(true)
                            .build())
                    .createdAt(LocalDateTime.now())
                    .build();

            eventPublisher.publishEvent(dashboardEvent);
        }

        return attendanceMapper.toDTO(attendance);
    }

    // ===== CHECK OUT =====
    @Transactional
    public ResAttendanceDTO checkOut() {

        String employeeIdStr = SecurityUtil.getCurrentEmployeeId();

        if (employeeIdStr == null) {
            throw new BadRequestException("EmployeeId not found in token");
        }

        UUID employeeId = UUID.fromString(employeeIdStr);

        LocalDate today = LocalDate.now();

        Attendance attendance = attendanceRepository
                .findByEmployeeIdAndWorkDate(employeeId, today)
                .orElseThrow(() -> new BadRequestException("Not checked in"));

        if (attendance.getCheckOutTime() != null) {
            throw new BadRequestException("Already checked out");
        }

        WorkShift shift = workShiftService.getEntityById(attendance.getShiftId());

        LocalDateTime checkOut = LocalDateTime.now();
        attendance.setCheckOutTime(checkOut);

        // ===== CALCULATE TOTAL WORKING HOURS =====
        double hours = Duration.between(
                attendance.getCheckInTime(),
                checkOut
        ).toMinutes() / 60.0;

        attendance.setTotalHours(hours);

        // ===== CALCULATE OVERTIME =====
        double overtime = Math.max(
                0,
                hours - shift.getStandardWorkHours()
        );

        attendance.setOvertimeHours(overtime);

        // ===== CHECK EARLY LEAVE =====
        boolean isEarlyLeave = checkOut.toLocalTime()
                .isBefore(shift.getEndTime());

        attendance.setEarlyLeave(isEarlyLeave);


        if (isEarlyLeave) {

            // If the previous value was LATE -> keep it as LATE
            if (attendance.getStatus() == AttendanceStatus.PRESENT) {
                attendance.setStatus(AttendanceStatus.EARLY_LEAVE);
            }

            // If previously set to LATE -> keep LATE
        }

        attendanceRepository.save(attendance);

        // Publish event to payroll-service
        eventPublisher.publishEvent(
                AttendanceRecordedEvent.builder()
                        .employeeId(employeeId)
                        .workDate(today)
                        .totalHours(hours)
                        .overtimeHours(overtime)
                        .status(attendance.getStatus())
                        .earlyLeave(attendance.isEarlyLeave())
                        .late(attendance.isLate())
                        .build()
        );

        return attendanceMapper.toDTO(attendance);
    }

    public ResultPaginationDTO listAttendances(
            UUID employeeId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) {

        UUID currentEmployeeId =
                UUID.fromString(SecurityUtil.getCurrentEmployeeId());

        Specification<Attendance> spec =
                AttendanceSpecification.byEmployee(employeeId)
                        .and(AttendanceSpecification.byDateRange(startDate, endDate));

        // ===== EMPLOYEE =====
        if (SecurityUtil.hasRole("ROLE_EMPLOYEE")) {

            spec = spec.and(
                    (root, query, cb) ->
                            cb.equal(root.get("employeeId"), currentEmployeeId)
            );
        }

        // ===== MANAGER =====
        if (SecurityUtil.hasRole("ROLE_MANAGER")) {

            List<UUID> subordinates =
                    employeeClient.getSubordinates(currentEmployeeId);

            spec = spec.and(
                    (root, query, cb) ->
                            root.get("employeeId").in(subordinates)
            );
        }

        Page<Attendance> page =
                attendanceRepository.findAll(spec, pageable);

        int pageNumber = pageable.getPageNumber() + 1;
        int pageSize = pageable.getPageSize();
        int totalPages = page.getTotalPages();
        long totalElements = page.getTotalElements();

        List<ResAttendanceDTO> list = page.getContent()
                .stream()
                .map(attendanceMapper::toDTO)
                .toList();

        return paginationMapper.convertToResultPaginationDTO(
                pageNumber, pageSize, totalPages, totalElements, list);
    }

    public ResultPaginationDTO listAttendancePersonal(
            UUID employeeId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) {

        UUID currentEmployeeId =
                UUID.fromString(SecurityUtil.getCurrentEmployeeId());

        Specification<Attendance> spec =
                AttendanceSpecification.byEmployee(employeeId)
                        .and(AttendanceSpecification.byDateRange(startDate, endDate));

        if (SecurityUtil.hasRole("ROLE_EMPLOYEE") || SecurityUtil.hasRole("ROLE_MANAGER")) {

            spec = spec.and(
                    (root, query, cb) ->
                            cb.equal(root.get("employeeId"), currentEmployeeId)
            );
        }


        Page<Attendance> page =
                attendanceRepository.findAll(spec, pageable);

        int pageNumber = pageable.getPageNumber() + 1;
        int pageSize = pageable.getPageSize();
        int totalPages = page.getTotalPages();
        long totalElements = page.getTotalElements();

        List<ResAttendanceDTO> list = page.getContent()
                .stream()
                .map(attendanceMapper::toDTO)
                .toList();

        return paginationMapper.convertToResultPaginationDTO(
                pageNumber, pageSize, totalPages, totalElements, list);
    }

    public ResAttendanceDTO getById(UUID id) {

        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Attendance not found"));

        UUID currentEmployeeId =
                UUID.fromString(SecurityUtil.getCurrentEmployeeId());

        // ===== EMPLOYEE =====
        if (SecurityUtil.hasRole("ROLE_EMPLOYEE")) {
            if (!attendance.getEmployeeId().equals(currentEmployeeId)) {
                throw new ForbiddenException("You cannot view this attendance");
            }
        }

        // ===== MANAGER =====
        if (SecurityUtil.hasRole("ROLE_MANAGER")) {

            List<UUID> subordinates =
                    employeeClient.getSubordinates(currentEmployeeId);

            if (!subordinates.contains(attendance.getEmployeeId())) {
                throw new ForbiddenException("You cannot view this attendance");
            }
        }

        return attendanceMapper.toDTO(attendance);
    }

}
