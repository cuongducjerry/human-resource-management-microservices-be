package com.hrm.payroll.service;

import com.hrm.payroll.dto.response.ResLeaveSummaryDTO;
import com.hrm.payroll.dto.response.ResultPaginationDTO;
import com.hrm.payroll.entity.LeaveSummary;
import com.hrm.payroll.event.LeaveApprovedEvent;
import com.hrm.payroll.event.LeaveCancelledEvent;
import com.hrm.payroll.mapper.LeaveSummaryMapper;
import com.hrm.payroll.mapper.PaginationMapper;
import com.hrm.payroll.repository.LeaveSummaryRepository;
import com.hrm.payroll.specification.LeaveSummarySpecification;
import com.hrm.payroll.util.error.IdInvalidException;
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
public class LeaveSummaryService {

    private final LeaveSummaryRepository leaveSummaryRepository;
    private final LeaveSummaryMapper leaveSummaryMapper;
    private final PaginationMapper paginationMapper;

    // ================= EVENT HANDLER =================
    @Transactional
    public void applyApprovedLeave(LeaveApprovedEvent event) {

        LocalDate date = event.getStartDate();

        int month = date.getMonthValue();
        int year = date.getYear();

        LeaveSummary summary =
                leaveSummaryRepository
                        .findByEmployeeIdAndMonthAndYear(
                                event.getEmployeeId(),
                                month,
                                year
                        )
                        .orElseGet(() ->
                                LeaveSummary.builder()
                                        .employeeId(event.getEmployeeId())
                                        .month(month)
                                        .year(year)
                                        .paidLeaveDays(0)
                                        .unpaidLeaveDays(0)
                                        .build()
                        );

        switch (event.getLeaveType()) {

            case ANNUAL, SICK, PATERNITY ->
                    summary.setPaidLeaveDays(
                            summary.getPaidLeaveDays() + event.getTotalDays()
                    );

            case MATERNITY, UNPAID ->
                    summary.setUnpaidLeaveDays(
                            summary.getUnpaidLeaveDays() + event.getTotalDays()
                    );
        }

        leaveSummaryRepository.save(summary);
    }

    // ================= ROLLBACK CANCEL =================
    @Transactional
    public void rollbackCancelledLeave(LeaveCancelledEvent event) {

        LocalDate date = event.getStartDate();

        int month = date.getMonthValue();
        int year = date.getYear();

        LeaveSummary summary =
                leaveSummaryRepository
                        .findByEmployeeIdAndMonthAndYear(
                                event.getEmployeeId(),
                                month,
                                year
                        )
                        .orElseThrow(() ->
                                new RuntimeException("Leave summary not found"));

        switch (event.getLeaveType()) {

            case ANNUAL, SICK, PATERNITY ->
                    summary.setPaidLeaveDays(
                            summary.getPaidLeaveDays() - event.getTotalDays()
                    );

            case MATERNITY, UNPAID ->
                    summary.setUnpaidLeaveDays(
                            summary.getUnpaidLeaveDays() - event.getTotalDays()
                    );
        }

        leaveSummaryRepository.save(summary);
    }

    // ================= LIST =================
    public ResultPaginationDTO list(
            UUID employeeId,
            Integer month,
            Integer year,
            Pageable pageable
    ) {

        Specification<LeaveSummary> spec =
                LeaveSummarySpecification
                        .filter(employeeId, month, year);

        Page<LeaveSummary> page =
                leaveSummaryRepository.findAll(spec, pageable);

        List<ResLeaveSummaryDTO> list =
                page.getContent()
                        .stream()
                        .map(leaveSummaryMapper::toDTO)
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
    public ResLeaveSummaryDTO getById(UUID id) {

        LeaveSummary entity =
                leaveSummaryRepository.findById(id)
                        .orElseThrow(() ->
                                new IdInvalidException("Leave summary not found"));

        return leaveSummaryMapper.toDTO(entity);
    }
}
