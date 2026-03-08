package com.hrm.notification.repository;

import com.hrm.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification> findByEmployeeIdAndActiveTrueOrderByCreatedAtDesc(UUID employeeId);


    @Modifying
    @Query("""
        update Notification n
        set n.isRead = true
        where n.employeeId = :employeeId
          and n.isRead = false
    """)
    void markAllAsRead(@Param("employeeId") UUID employeeId);

    Optional<Notification> findById(UUID id);

}
