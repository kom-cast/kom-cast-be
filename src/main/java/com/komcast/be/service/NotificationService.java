package com.komcast.be.service;

import com.komcast.be.domain.Notification;
import com.komcast.be.domain.User;
import com.komcast.be.dto.NotificationResponseDto;
import com.komcast.be.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final PreferenceService preferenceService;

    @Transactional
    public List<NotificationResponseDto> getNotifications(Long userId) {
        User user = preferenceService.getOrCreateUser(userId);
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        if (notifications.isEmpty()) {
            createDummyNotifications(user);
            notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        }

        return notifications.stream()
                .map(n -> NotificationResponseDto.builder()
                        .id(n.getId())
                        .type(n.getType())
                        .title(n.getTitle())
                        .description(n.getDescription())
                        .time("방금 전")
                        .unread(!n.getIsRead())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));
        notification.markAsRead();
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        User user = preferenceService.getOrCreateUser(userId);
        List<Notification> unreadList = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(user.getId());
        for (Notification n : unreadList) {
            n.markAsRead();
        }
    }

    private void createDummyNotifications(User user) {
        notificationRepository.save(Notification.builder()
                .user(user)
                .type("BRIEFING")
                .title("오늘의 브리핑이 준비됐어요")
                .description("새로운 맞춤형 아침 브리핑을 들어보세요")
                .isRead(false)
                .build());

        notificationRepository.save(Notification.builder()
                .user(user)
                .type("STOCK_ALERT")
                .title("삼성전자 +5% 급등")
                .description("보유종목 삼성전자가 급등했습니다")
                .isRead(false)
                .build());

        notificationRepository.save(Notification.builder()
                .user(user)
                .type("VOICE_UPDATE")
                .title("새로운 목소리가 추가됐어요")
                .description("마이페이지에서 새 목소리를 들어보세요")
                .isRead(true)
                .build());
    }
}
