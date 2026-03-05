package com.hrm.attendance.scheduler;

import com.hrm.attendance.client.EmployeeClient;
import com.hrm.attendance.entity.Attendance;
import com.hrm.attendance.repository.AttendanceRepository;
import com.hrm.attendance.util.constant.AttendanceStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AttendanceScheduler {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeClient employeeClient;

    @Scheduled(cron = "0 59 23 * * ?")
    @Transactional
    public void markAbsentEmployees() {

        LocalDate today = LocalDate.now();

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
                        .build();

                attendanceRepository.save(absent);
            }
        }
    }
}
