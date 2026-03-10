package com.hrm.leave.service;

import com.hrm.leave.client.EmployeeClient;
import com.hrm.leave.entity.LeaveRequest;
import com.hrm.leave.event.NotificationEvent;
import com.hrm.leave.repository.LeaveRequestRepository;
import com.hrm.leave.util.constant.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeaveEscalationService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeClient employeeClient;
    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(cron = "0 0 * * * *") // every 1 hour
    @Transactional
    public void escalatePendingLeaves() {

        Instant threshold = Instant.now().minus(24, ChronoUnit.HOURS);

        List<LeaveRequest> leaves =
                leaveRequestRepository.findPendingForEscalation(threshold);

        if (leaves.isEmpty()) {
            return;
        }

        // Get list HR_ADMIN
        List<UUID> hrAdmins = employeeClient.getHrAdminIds();

        for (LeaveRequest leave : leaves) {

            leave.setEscalated(true);

            // Send notifications to each HR_ADMIN
            for (UUID hrId : hrAdmins) {

                NotificationEvent event = NotificationEvent.builder()
                        .eventId(UUID.randomUUID().toString())
                        .employeeId(hrId.toString())
                        .title("Leave request escalation")
                        .content("Leave request " + leave.getId()
                                + " has not been approved by manager after 24 hours.")
                        .type(NotificationType.LEAVE)
                        .sendEmail(true)     ////////////////////////////////////////
                        .build();

                eventPublisher.publishEvent(event);
            }

            System.out.println("Escalated leave: " + leave.getId());
        }

        leaveRequestRepository.saveAll(leaves);
    }
}
