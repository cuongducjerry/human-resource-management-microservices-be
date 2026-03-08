package com.hrm.notification.service;

import com.hrm.notification.util.constant.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationCacheService {

    private final RedisTemplate<String, Object> redis;

    /* =====================
       INCREASE / RESET
       ===================== */

    public void increaseUnread(String userId, NotificationType type) {
        redis.opsForValue().increment(key(userId, type));
    }

    public void resetUnread(String userId, NotificationType type) {
        redis.opsForValue().set(key(userId, type), 0);
    }

    public void decreaseUnread(String userId, NotificationType type) {
        String key = key(userId, type);
        Object val = redis.opsForValue().get(key);

        long current = (val == null) ? 0L : ((Number) val).longValue();

        if (current <= 0) {
            redis.opsForValue().set(key, 0);
        } else {
            redis.opsForValue().set(key, current - 1);
            // or redis.opsForValue().increment(key, -1);
        }
    }

    /* =====================
       GET UNREAD
       ===================== */

    public long getUnread(String userId, NotificationType type) {
        Object val = redis.opsForValue().get(key(userId, type));
        return val == null ? 0L : Long.parseLong(val.toString());
    }

    public long getTotalUnread(String userId) {

        long total = 0;

        for (NotificationType type : NotificationType.values()) {
            total += getUnread(userId, type);
        }

        return total;
    }


    /* =====================
       KEY BUILDER
       ===================== */

    private String key(String userId, NotificationType type) {
        return "noti-hrm:unread:user:" + userId + ":" + type.name();
    }

    public boolean markEventProcessed(String eventId) {
        String key = "noti-hrm:event:" + eventId;
        Boolean ok = redis.opsForValue()
                .setIfAbsent(key, 1, Duration.ofMinutes(10));
        return Boolean.TRUE.equals(ok);
    }


}

