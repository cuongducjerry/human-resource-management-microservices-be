package com.hrm.attendance.service;

import com.hrm.attendance.client.EmployeeClient;
import com.hrm.attendance.dto.response.ResAttendanceDTO;
import com.hrm.attendance.dto.response.ResultPaginationDTO;
import com.hrm.attendance.entity.Attendance;
import com.hrm.attendance.entity.WorkShift;
import com.hrm.attendance.mapper.AttendanceMapper;
import com.hrm.attendance.mapper.PaginationMapper;
import com.hrm.attendance.repository.AttendanceRepository;
import com.hrm.attendance.specification.AttendanceSpecification;
import com.hrm.attendance.util.SecurityUtil;
import com.hrm.attendance.util.constant.AttendanceStatus;
import com.hrm.attendance.util.error.BadRequestException;
import com.hrm.attendance.util.error.IdInvalidException;
import lombok.RequiredArgsConstructor;
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

        AttendanceStatus status = AttendanceStatus.PRESENT;

        if (checkInTime.isAfter(
                shift.getStartTime()
                        .plusMinutes(shift.getAllowLateMinutes()))) {
            status = AttendanceStatus.LATE;
        }

        Attendance attendance = Attendance.builder()
                .employeeId(employeeId)
                .shiftId(shiftId)  // snapshot
                .workDate(today)
                .checkInTime(now)
                .status(status)
                .build();

        attendanceRepository.save(attendance);

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
        if (checkOut.toLocalTime()
                .isBefore(shift.getEndTime())) {

            attendance.setStatus(AttendanceStatus.EARLY_LEAVE);
        }

        attendanceRepository.save(attendance);

        // Publish event cho payroll-service
//        eventPublisher.publishEvent(
//                AttendanceRecordedEvent.builder()
//                        .employeeId(employeeId)
//                        .workDate(today)
//                        .totalHours(hours)
//                        .overtimeHours(overtime)
//                        .build()
//        );

        return attendanceMapper.toDTO(attendance);
    }

    public ResultPaginationDTO listAttendances(
            UUID employeeId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) {

        Specification<Attendance> spec =
                AttendanceSpecification.byEmployee(employeeId)
                        .and(AttendanceSpecification.byDateRange(startDate, endDate));

        Page<Attendance> page = attendanceRepository.findAll(spec, pageable);

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

        return attendanceMapper.toDTO(attendance);
    }

}
