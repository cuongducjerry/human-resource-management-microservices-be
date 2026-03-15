package com.hrm.auth.event;

import com.hrm.auth.util.constant.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

