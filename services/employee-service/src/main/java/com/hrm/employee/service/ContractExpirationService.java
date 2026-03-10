package com.hrm.employee.service;

import com.hrm.employee.entity.Contract;
import com.hrm.employee.event.NotificationEvent;
import com.hrm.employee.repository.ContractRepository;
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
public class ContractExpirationService {

    private final ContractRepository contractRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final EmployeeService employeeService;

    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void notifyExpiringContracts() {

        LocalDate today = LocalDate.now();
        LocalDate threshold = today.plusDays(7);

        List<Contract> contracts =
                contractRepository.findContractsExpiringBetween(today, threshold);

        List<UUID> hrAdmins = employeeService.getHrAdminIds();

        for (Contract contract : contracts) {

            for (UUID hrId : hrAdmins) {

                NotificationEvent event = NotificationEvent.builder()
                        .eventId(UUID.randomUUID().toString())
                        .employeeId(hrId.toString())
                        .title("Contract expiring soon")
                        .content("Contract of employee " + contract.getEmployeeId()
                                + " will expire on " + contract.getEndDate())
                        .type(NotificationType.SYSTEM)
                        .sendEmail(true)
                        .build();

                eventPublisher.publishEvent(event);
            }
        }
    }
}
