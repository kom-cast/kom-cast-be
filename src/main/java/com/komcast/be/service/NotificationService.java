package com.komcast.be.service;

import com.komcast.be.domain.Notification;
import com.komcast.be.domain.User;
import com.komcast.be.dto.NotificationResponseDto;
import com.komcast.be.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final PreferenceService preferenceService;

    @Transactional
    public List<NotificationResponseDto> getNotifications(Object userId) {
        User user = preferenceService.getOrCreateUser(userId);
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        return notifications.stream()
                .map(n -> NotificationResponseDto.builder()
                        .id(n.getId() != null ? n.getId().toString() : UUID.randomUUID().toString())
                        .type(n.getType())
                        .title(n.getTitle())
                        .description(n.getDescription())
                        .time(formatRelativeTime(n.getCreatedAt()))
                        .unread(!n.getIsRead())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(Object userId, Object notificationId) {
        if (notificationId != null) {
            try {
                UUID nId = notificationId instanceof UUID ? (UUID) notificationId : UUID.fromString(notificationId.toString());
                Notification notification = notificationRepository.findById(nId).orElse(null);
                if (notification != null) {
                    notification.markAsRead();
                }
            } catch (Exception ignored) {}
        }
    }

    @Transactional
    public void markAllAsRead(Object userId) {
        User user = preferenceService.getOrCreateUser(userId);
        List<Notification> unreadList = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(user.getId());
        for (Notification n : unreadList) {
            n.markAsRead();
        }
    }

    private String formatRelativeTime(java.time.LocalDateTime createdAt) {
        if (createdAt == null) {
            return "방금 전";
        }
        java.time.Duration duration = java.time.Duration.between(createdAt, java.time.LocalDateTime.now());
        long seconds = duration.getSeconds();

        if (seconds < 60) {
            return "방금 전";
        } else if (seconds < 3600) {
            return (seconds / 60) + "분 전";
        } else if (seconds < 86400) {
            return (seconds / 3600) + "시간 전";
        } else {
            return (seconds / 86400) + "일 전";
        }
    }
}
