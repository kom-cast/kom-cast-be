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
                        .time("방금 전")
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
}
