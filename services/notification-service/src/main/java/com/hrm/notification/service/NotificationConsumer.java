package com.hrm.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrm.notification.dto.response.ResNotiDTO;
import com.hrm.notification.entity.Notification;
import com.hrm.notification.event.NotificationEvent;
import com.hrm.notification.repository.NotificationRepository;
import com.hrm.notification.util.constant.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationRepository repo;
    private final NotificationCacheService cache;
    private final SimpMessagingTemplate ws;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopics.NOTIFICATION_TOPIC, groupId = "notification-group")
    public void handle(String message) throws Exception {

        System.out.println("RAW JSON: " + message);

        NotificationEvent event =
                objectMapper.readValue(message, NotificationEvent.class);

        // IDempotency guard
        if (!cache.markEventProcessed(event.getEventId())) {
            System.out.println("Duplicate event: " + event.getEventId());
            return;
        }

        // CASE 1
        if (event.getEmployeeId() == null || event.getEmployeeId().isBlank()) {

            if (event.isSendEmail() && event.getEmail() != null) {
                emailService.send(
                        event.getEmail(),
                        event.getTitle(),
                        event.getContent()
                );
            }

            return;
        }

        // CASE 2
        Notification noti = Notification.builder()
                .title(event.getTitle())
                .content(event.getContent())
                .type(event.getType())
                .employeeId(UUID.fromString(event.getEmployeeId()))
                .isRead(false)
                .active(true)
                .build();

        repo.saveAndFlush(noti);

        cache.increaseUnread(event.getEmployeeId(), event.getType());

        ResNotiDTO dto = ResNotiDTO.builder()
                .id(noti.getId())
                .title(noti.getTitle())
                .content(noti.getContent())
                .type(noti.getType())
                .isRead(false)
                .createdAt(noti.getCreatedAt())
                .build();

        ws.convertAndSendToUser(
                event.getEmployeeId(),
                "/queue/notifications",
                dto
        );

        try {
            if (event.isSendEmail() && event.getEmail() != null) {
                emailService.send(
                        event.getEmail(),
                        event.getTitle(),
                        event.getContent()
                );
            }
        } catch (Exception e) {
            log.error("Email send failed for event {}", event.getEventId(), e);
        }

    }
}
