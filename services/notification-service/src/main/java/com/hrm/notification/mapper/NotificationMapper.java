package com.hrm.notification.mapper;

import com.hrm.notification.dto.response.ResNotiDTO;
import com.hrm.notification.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public static ResNotiDTO toResDTO(Notification n) {
        return ResNotiDTO.builder()
                .id(n.getId())
                .title(n.getTitle())
                .content(n.getContent())
                .type(n.getType())
                .isRead(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }

}
