package com.hrm.notification.controller;

import com.hrm.notification.dto.response.ResNotiDTO;
import com.hrm.notification.service.NotificationEmployeeService;
import com.hrm.notification.util.annotation.ApiMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationEmployeeService notificationEmployeeService;

    @GetMapping
    @ApiMessage("Get my notifications")
    public ResponseEntity<List<ResNotiDTO>> listNotifications() {
        return ResponseEntity.ok(notificationEmployeeService.getMyNotifications());
    }

    @GetMapping("/unread")
    @ApiMessage("Get total unread notifications")
    public ResponseEntity<Long> unreadNotifications() {
        return ResponseEntity.ok(notificationEmployeeService.getTotalUnread());
    }

    @PostMapping("/read-all")
    @ApiMessage("Mark all notifications as read")
    public ResponseEntity<Void> readAllNotifications() {
        notificationEmployeeService.readAll();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/read")
    @ApiMessage("Mark one notification as read")
    public ResponseEntity<Void> readOne(@PathVariable String id) {
        notificationEmployeeService.readOne(id);
        return ResponseEntity.ok().build();
    }
}
