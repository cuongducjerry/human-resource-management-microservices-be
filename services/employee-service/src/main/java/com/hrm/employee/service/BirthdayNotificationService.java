package com.hrm.employee.service;

import com.hrm.employee.entity.Employee;
import com.hrm.employee.event.NotificationEvent;
import com.hrm.employee.repository.EmployeeRepository;
import com.hrm.employee.util.constant.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BirthdayNotificationService {

    private final EmployeeRepository employeeRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(cron = "0 0 8 * * *")
    @Transactional(readOnly = true)
    public void sendBirthdayNotification() {

        LocalDate today = LocalDate.now();

        List<Employee> employees =
                employeeRepository.findByMonthAndDay(
                        today.getMonthValue(),
                        today.getDayOfMonth()
                );

        for (Employee employee : employees) {

            NotificationEvent event = NotificationEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .employeeId(employee.getId().toString())
                    .title("Happy Birthday")
                    .content("Happy birthday " + employee.getFullName() + "! Have a great day!")
                    .type(NotificationType.SYSTEM)
                    .sendEmail(false)
                    .build();

            eventPublisher.publishEvent(event);
        }
    }
}
