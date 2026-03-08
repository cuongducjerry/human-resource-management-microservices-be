package com.hrm.notification.dto.response;

import com.hrm.notification.util.constant.NotificationType;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResNotiDTO {

    private UUID id;

    private String title;

    private String content;

    private NotificationType type;

    private boolean isRead;

    private Instant createdAt;

}
