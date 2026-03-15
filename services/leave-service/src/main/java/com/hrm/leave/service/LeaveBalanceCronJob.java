package com.hrm.leave.service;

import com.hrm.leave.client.EmployeeClient;
import com.hrm.leave.entity.LeaveBalance;
import com.hrm.leave.repository.LeaveBalanceRepository;
import com.hrm.leave.util.constant.LeaveType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class LeaveBalanceCronJob {

    private final LeaveBalanceRepository leaveBalanceRepository;
    private final EmployeeClient employeeClient;

    @Scheduled(cron = "0 0 0 1 1 *") // 00:00 - 1/1
    @Transactional
    public void generateNewYearLeaveBalance() {

        int year = Year.now().getValue();
        int lastYear = year - 1;

        log.info("START GENERATE LEAVE BALANCE FOR YEAR {}", year);

        // deactivate the old balance
        leaveBalanceRepository.deactivateByYear(lastYear);

        // Get employeeId from employee-service
        List<UUID> employeeIds = employeeClient.getAllActiveEmployeeIds();

        if (employeeIds == null || employeeIds.isEmpty()) {
            log.warn("No employees found");
            return;
        }

        // create new balance
        for (UUID employeeId : employeeIds) {

            for (LeaveType type : LeaveType.values()) {

                if (type == LeaveType.UNPAID) continue;

                boolean exists =
                        leaveBalanceRepository.existsByEmployeeIdAndLeaveTypeAndYear(
                                employeeId, type, year
                        );

                if (exists) continue;

                Integer totalDays = resolveDefaultDays(type);

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

        log.info("FINISH GENERATE LEAVE BALANCE FOR YEAR {}", year);
    }

    private Integer resolveDefaultDays(LeaveType type) {

        return switch (type) {
            case ANNUAL -> 12;
            case SICK -> 10;
            case MATERNITY -> 180;
            case PATERNITY -> 5;
            default -> null;
        };
    }
}