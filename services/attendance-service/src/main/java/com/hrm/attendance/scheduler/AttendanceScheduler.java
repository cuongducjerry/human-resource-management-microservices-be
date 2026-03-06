package com.hrm.attendance.scheduler;

import com.hrm.attendance.client.EmployeeClient;
import com.hrm.attendance.entity.Attendance;
import com.hrm.attendance.event.AttendanceRecordedEvent;
import com.hrm.attendance.repository.AttendanceRepository;
import com.hrm.attendance.util.constant.AttendanceStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AttendanceScheduler {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeClient employeeClient;
    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(cron = "0 59 23 * * ?")
    @Transactional
    public void markAbsentEmployees() {

        LocalDate today = LocalDate.now();

        DayOfWeek dayOfWeek = today.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SUNDAY) {
            return;
        }

        List<UUID> allEmployees = employeeClient.getAllActiveEmployeeIds();

        for (UUID empId : allEmployees) {

            boolean exists = attendanceRepository
                    .findByEmployeeIdAndWorkDate(empId, today)
                    .isPresent();

            if (!exists) {
                Attendance absent = Attendance.builder()
                        .employeeId(empId)
                        .workDate(today)
                        .status(AttendanceStatus.ABSENT)
                        .totalHours(0.0)
                        .overtimeHours(0.0)
                        .late(false)
                        .earlyLeave(false)
                        .late(false)
                        .build();

                attendanceRepository.save(absent);

                // ===== PUBLISH EVENT =====
                eventPublisher.publishEvent(
                        AttendanceRecordedEvent.builder()
                                .employeeId(absent.getEmployeeId())
                                .workDate(absent.getWorkDate())
                                .totalHours(absent.getTotalHours())
                                .overtimeHours(absent.getOvertimeHours())
                                .status(absent.getStatus())
                                .late(absent.isLate())
                                .build()
                );
            }
        }
    }
}
