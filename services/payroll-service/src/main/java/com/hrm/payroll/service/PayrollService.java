package com.hrm.payroll.service;

import com.hrm.payroll.client.EmployeeClient;
import com.hrm.payroll.dto.response.ResContractDTO;
import com.hrm.payroll.dto.response.ResPayrollDTO;
import com.hrm.payroll.dto.response.ResultPaginationDTO;
import com.hrm.payroll.entity.AttendanceSummary;
import com.hrm.payroll.entity.LeaveSummary;
import com.hrm.payroll.entity.Payroll;
import com.hrm.payroll.event.NotificationEvent;
import com.hrm.payroll.mapper.PaginationMapper;
import com.hrm.payroll.mapper.PayrollMapper;
import com.hrm.payroll.repository.AttendanceSummaryRepository;
import com.hrm.payroll.repository.LeaveSummaryRepository;
import com.hrm.payroll.repository.PayrollRepository;
import com.hrm.payroll.specification.PayrollSpecification;
import com.hrm.payroll.util.SecurityUtil;
import com.hrm.payroll.util.constant.NotificationType;
import com.hrm.payroll.util.constant.PayrollStatus;
import com.hrm.payroll.util.error.BadRequestException;
import com.hrm.payroll.util.error.ForbiddenException;
import com.hrm.payroll.util.error.IdInvalidException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PayrollService {

    private final PayrollRepository payrollRepository;
    private final PayrollMapper payrollMapper;
    private final PaginationMapper paginationMapper;
    private final EmployeeClient employeeClient;
    private final AttendanceSummaryRepository attendanceSummaryRepository;
    private final LeaveSummaryRepository leaveSummaryRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ResultPaginationDTO list(
            UUID employeeId,
            Integer month,
            Integer year,
            PayrollStatus status,
            Pageable pageable
    ) {

        // nếu user là EMPLOYEE thì chỉ được xem payroll của mình
        if (SecurityUtil.hasRole("ROLE_EMPLOYEE")) {
            employeeId = UUID.fromString(SecurityUtil.getCurrentEmployeeId());
        }

        Specification<Payroll> spec =
                PayrollSpecification.filter(
                        employeeId,
                        month,
                        year,
                        status
                );

        Page<Payroll> page =
                payrollRepository.findAll(spec, pageable);

        List<ResPayrollDTO> list =
                page.getContent()
                        .stream()
                        .map(payrollMapper::toDTO)
                        .toList();

        return paginationMapper.convertToResultPaginationDTO(
                pageable.getPageNumber() + 1,
                pageable.getPageSize(),
                page.getTotalPages(),
                page.getTotalElements(),
                list
        );
    }

    // calculate all
    @Transactional
    public void calculateAll(Integer month, Integer year) {

        List<Payroll> payrolls =
                payrollRepository.findByMonthAndYear(month, year);

        for (Payroll payroll : payrolls) {

            if (payroll.getStatus() == PayrollStatus.DRAFT) {

                payroll.setStatus(PayrollStatus.CALCULATED);

                sendPayrollNotification(
                        payroll,
                        "Your payroll for " + month + "/" + year + " is calculated."
                );
            }
        }

        payrollRepository.saveAll(payrolls);
    }

    // paid all
    @Transactional
    public void markAllAsPaid(Integer month, Integer year) {

        List<Payroll> payrolls =
                payrollRepository.findByMonthAndYear(month, year);

        for (Payroll payroll : payrolls) {

            if (payroll.getStatus() == PayrollStatus.CALCULATED) {

                payroll.setStatus(PayrollStatus.PAID);

                sendPayrollNotification(
                        payroll,
                        "Your salary for " + month + "/" + year + " has been paid."
                );
            }
        }

        payrollRepository.saveAll(payrolls);
    }

    @Transactional
    public List<ResPayrollDTO> generatePayrollForMonth(int month, int year) {

        List<UUID> employeeIds = employeeClient.getAllActiveEmployeeIds();

        List<ResPayrollDTO> result = new ArrayList<>();

        for (UUID employeeId : employeeIds) {

            Payroll payroll =
                    generatePayroll(employeeId, month, year);

            result.add(payrollMapper.toDTO(payroll));

        }

        return result;
    }

    @Transactional
    public Payroll generatePayroll(UUID employeeId, int month, int year) {

        // 1. Check payroll already exists.
        Optional<Payroll> existing =
                payrollRepository.findByEmployeeIdAndMonthAndYear(employeeId, month, year);

        if (existing.isPresent()) {
            return existing.get();
        }

        // 2. Get contract
        ResContractDTO contract =
                employeeClient.getActiveContract(employeeId);

        if (contract == null || contract.getSalary() == null) {
            throw new BadRequestException("Employee contract or salary not found");
        }

        // 3. Attendance summary
        AttendanceSummary attendance =
                attendanceSummaryRepository
                        .findByEmployeeIdAndMonthAndYear(employeeId, month, year)
                        .orElseGet(() ->
                                AttendanceSummary.builder()
                                        .overtimeHours(0.0)
                                        .build()
                        );

        // 4. Leave summary
        LeaveSummary leave =
                leaveSummaryRepository
                        .findByEmployeeIdAndMonthAndYear(employeeId, month, year)
                        .orElseGet(() ->
                                LeaveSummary.builder()
                                        .unpaidLeaveDays(0)
                                        .build()
                        );

        // 5. Base salary
        BigDecimal baseSalary = contract.getSalary();

        // 6. Salary per day
        BigDecimal salaryPerDay =
                baseSalary.divide(BigDecimal.valueOf(26), 2, RoundingMode.HALF_UP);

        // 7. Salary per hour
        BigDecimal salaryPerHour =
                baseSalary.divide(BigDecimal.valueOf(26 * 8), 2, RoundingMode.HALF_UP);

        // 8. Unpaid leave
        int unpaidLeaveDays =
                leave.getUnpaidLeaveDays() == null ? 0 : leave.getUnpaidLeaveDays();

        System.out.println("============================= unpaid ===============================");
        System.out.println(unpaidLeaveDays);

        // 9. Overtime hours
        double overtimeHours =
                attendance.getOvertimeHours() == null ? 0 : attendance.getOvertimeHours();

        // 10. Deduction
        BigDecimal deduction =
                salaryPerDay.multiply(BigDecimal.valueOf(unpaidLeaveDays));

        // 11. Overtime pay (150%)
        BigDecimal overtimePay =
                salaryPerHour
                        .multiply(BigDecimal.valueOf(overtimeHours))
                        .multiply(BigDecimal.valueOf(1.5));

        // 12. Net salary
        BigDecimal netSalary =
                baseSalary
                        .add(overtimePay)
                        .subtract(deduction);

        // 13. Create payroll
        Payroll payroll = Payroll.builder()
                .employeeId(employeeId)
                .month(month)
                .year(year)
                .baseSalary(baseSalary)
                .overtimePay(overtimePay)
                .deduction(deduction)
                .netSalary(netSalary)
                .status(PayrollStatus.DRAFT)
                .build();

        return payrollRepository.save(payroll);
    }

    public ResPayrollDTO getPayrollDetail(UUID payrollId) {

        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new RuntimeException("Payroll not found"));

        String employeeId = SecurityUtil.getCurrentEmployeeId();

        // If the user is an EMPLOYEE, only their own payroll will be displayed
        if (SecurityUtil.hasRole("ROLE_EMPLOYEE") &&
                !payroll.getEmployeeId().toString().equals(employeeId)) {

            throw new ForbiddenException("You cannot view other employee payroll");
        }

        return payrollMapper.toDTO(payroll);
    }

    @Transactional
    public ResPayrollDTO calculatePayroll(UUID payrollId) {

        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new IdInvalidException("Payroll not found"));

        if (payroll.getStatus() == PayrollStatus.DRAFT) {

            payroll.setStatus(PayrollStatus.CALCULATED);
            payrollRepository.save(payroll);

            sendPayrollNotification(
                    payroll,
                    "Your payroll for " + payroll.getMonth() + "/" + payroll.getYear() + " is calculated."
            );
        }

        return payrollMapper.toDTO(payroll);
    }

    @Transactional
    public ResPayrollDTO markAsPaid(UUID payrollId) {

        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new RuntimeException("Payroll not found"));

        if (payroll.getStatus() == PayrollStatus.CALCULATED) {

            payroll.setStatus(PayrollStatus.PAID);
            payrollRepository.save(payroll);

            sendPayrollNotification(
                    payroll,
                    "Your salary for " + payroll.getMonth() + "/" + payroll.getYear() + " has been paid."
            );
        }

        return payrollMapper.toDTO(payroll);
    }

    private void sendPayrollNotification(Payroll payroll, String message) {

        NotificationEvent event = NotificationEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .employeeId(payroll.getEmployeeId().toString())
                .title("Payroll Update")
                .content(message)
                .type(NotificationType.PAYROLL)
                .sendEmail(true)
                .build();

        eventPublisher.publishEvent(event);
    }

}
