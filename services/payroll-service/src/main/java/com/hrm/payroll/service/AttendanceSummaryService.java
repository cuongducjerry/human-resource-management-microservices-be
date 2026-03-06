package com.hrm.payroll.service;

import com.hrm.payroll.dto.response.ResAttendanceSummaryDTO;
import com.hrm.payroll.dto.response.ResultPaginationDTO;
import com.hrm.payroll.entity.AttendanceSummary;
import com.hrm.payroll.event.AttendanceRecordedEvent;
import com.hrm.payroll.mapper.AttendanceSummaryMapper;
import com.hrm.payroll.mapper.PaginationMapper;
import com.hrm.payroll.repository.AttendanceSummaryRepository;
import com.hrm.payroll.specification.AttendanceSummarySpecification;
import com.hrm.payroll.util.constant.AttendanceStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttendanceSummaryService {

    private final AttendanceSummaryRepository attendanceSummaryRepository;
    private final AttendanceSummaryMapper attendanceSummaryMapper;
    private final PaginationMapper paginationMapper;

    // ================= EVENT HANDLER =================
    @Transactional
    public void initAttendanceSummary(AttendanceRecordedEvent event) {

        LocalDate date = event.getWorkDate();

        int month = date.getMonthValue();
        int year = date.getYear();

        AttendanceSummary summary =
                attendanceSummaryRepository
                        .findByEmployeeIdAndMonthAndYear(
                                event.getEmployeeId(),
                                month,
                                year
                        )
                        .orElseGet(() ->
                                AttendanceSummary.builder()
                                        .employeeId(event.getEmployeeId())
                                        .month(month)
                                        .year(year)
                                        .workingDays(0)
                                        .lateDays(0)
                                        .absentDays(0)
                                        .totalWorkHours(0.0)
                                        .overtimeHours(0.0)
                                        .build()
                        );

        // ===== ABSENT =====
        if (event.getStatus() == AttendanceStatus.ABSENT) {
            summary.setAbsentDays(summary.getAbsentDays() + 1);
        }
        else {

            // ===== WORKING DAY =====
            summary.setWorkingDays(summary.getWorkingDays() + 1);

            // ===== LATE =====
            if (event.isLate()) {
                summary.setLateDays(summary.getLateDays() + 1);
            }
        }

        // ===== WORK HOURS =====
        summary.setTotalWorkHours(
                summary.getTotalWorkHours() + event.getTotalHours()
        );

        // ===== OVERTIME =====
        summary.setOvertimeHours(
                summary.getOvertimeHours() + event.getOvertimeHours()
        );

        attendanceSummaryRepository.save(summary);
    }

    // ================= LIST =================
    public ResultPaginationDTO list(
            UUID employeeId,
            Integer month,
            Integer year,
            Pageable pageable
    ) {

        Specification<AttendanceSummary> spec =
                AttendanceSummarySpecification
                        .filter(employeeId, month, year);

        Page<AttendanceSummary> page =
                attendanceSummaryRepository.findAll(spec, pageable);

        List<ResAttendanceSummaryDTO> list =
                page.getContent()
                        .stream()
                        .map(attendanceSummaryMapper::toDTO)
                        .toList();

        return paginationMapper.convertToResultPaginationDTO(
                pageable.getPageNumber() + 1,
                pageable.getPageSize(),
                page.getTotalPages(),
                page.getTotalElements(),
                list
        );
    }

    // ================= DETAIL =================
    public ResAttendanceSummaryDTO getById(UUID id) {

        AttendanceSummary entity =
                attendanceSummaryRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException("Summary not found"));

        return attendanceSummaryMapper.toDTO(entity);
    }

}
