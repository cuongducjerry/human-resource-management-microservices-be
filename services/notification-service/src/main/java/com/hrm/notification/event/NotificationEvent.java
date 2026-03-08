package com.hrm.notification.event;

import com.hrm.notification.util.constant.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationEvent {

    private String eventId;

    private String employeeId;

    private String email;

    private NotificationType type;

    private String title;

    private String content;

    private boolean sendEmail;
}
