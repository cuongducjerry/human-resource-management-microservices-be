package com.hrm.notification.service;

import com.hrm.notification.dto.response.ResNotiDTO;
import com.hrm.notification.entity.Notification;
import com.hrm.notification.mapper.NotificationMapper;
import com.hrm.notification.repository.NotificationRepository;
import com.hrm.notification.util.SecurityUtil;
import com.hrm.notification.util.constant.NotificationType;
import com.hrm.notification.util.error.ForbiddenException;
import com.hrm.notification.util.error.IdInvalidException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationEmployeeService {

    private final NotificationRepository repo;
    private final NotificationCacheService cache;

    public List<ResNotiDTO> getMyNotifications() {

        String employeeId = SecurityUtil.getCurrentEmployeeId();

        UUID uuid = UUID.fromString(employeeId);

        return repo.findByEmployeeIdAndActiveTrueOrderByCreatedAtDesc(uuid)
                .stream()
                .map(NotificationMapper::toResDTO)
                .toList();
    }

    public long getTotalUnread() {
        return cache.getTotalUnread(SecurityUtil.getCurrentEmployeeId());
    }

    @Transactional
    public void readAll() {

        String employeeId = SecurityUtil.getCurrentEmployeeId();

        UUID uuid = UUID.fromString(employeeId);

        repo.markAllAsRead(uuid);

        for (NotificationType t : NotificationType.values()) {
            cache.resetUnread(employeeId, t);
        }
    }

    @Transactional
    public void readOne(String notiId) {

        String employeeId = SecurityUtil.getCurrentEmployeeId();

        UUID uuid = UUID.fromString(notiId);

        Notification noti = repo.findById(uuid)
                .orElseThrow(() -> new IdInvalidException("Notification not found"));

        if (!noti.getEmployeeId().toString().equals(employeeId)) {
            throw new ForbiddenException("Forbidden");
        }

        if (!noti.isRead()) {
            noti.setRead(true);
            repo.save(noti);
            cache.decreaseUnread(employeeId, noti.getType());
        }
    }

}
