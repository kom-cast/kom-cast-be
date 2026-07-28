package com.komcast.be;

import com.komcast.be.domain.Notification;
import com.komcast.be.domain.User;
import com.komcast.be.domain.UserPlan;
import com.komcast.be.dto.NotificationResponseDto;
import com.komcast.be.dto.NotificationToggleRequestDto;
import com.komcast.be.dto.PreferencesResponseDto;
import com.komcast.be.repository.NotificationRepository;
import com.komcast.be.repository.UserRepository;
import com.komcast.be.service.NotificationService;
import com.komcast.be.service.PreferenceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PreferenceAndNotificationServiceTest {

    @Autowired
    private PreferenceService preferenceService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("환경설정 알림 토글 업데이트 테스트")
    void updateNotifications_success() {
        Object userId = 1L;

        // 1. 알림 설정 업데이트
        preferenceService.updateNotifications(userId, NotificationToggleRequestDto.builder()
                .notifyBriefing(false)
                .notifyPriceAlert(false)
                .notifyMarketing(true)
                .build());

        // 2. 조회 및 검증
        PreferencesResponseDto pref = preferenceService.getPreferences(userId);
        assertThat(pref.getNotifyBriefing()).isFalse();
        assertThat(pref.getNotifyPriceAlert()).isFalse();
        assertThat(pref.getNotifyMarketing()).isTrue();
    }

    @Test
    @DisplayName("알림 목록 조회 시 고정된 DB ID가 유지되어야 하며 읽음 처리가 반영되어야 함")
    void getNotifications_maintainsConsistentIdsAndReadState() {
        Object userId = 1L;

        User user = preferenceService.getOrCreateUser(userId);
        notificationRepository.save(Notification.builder()
                .user(user)
                .type("BRIEFING")
                .title("테스트 알림")
                .description("내용")
                .isRead(false)
                .build());

        // 1. 첫 조회 시 DB에 저장 및 ID 부여
        List<NotificationResponseDto> list1 = notificationService.getNotifications(userId);
        assertThat(list1).isNotEmpty();
        Object firstId = list1.get(0).getId();

        // 2. 두 번째 조회 시에도 동일한 ID 유지
        List<NotificationResponseDto> list2 = notificationService.getNotifications(userId);
        assertThat(list2.get(0).getId()).isEqualTo(firstId);

        // 3. 읽음 처리 수행
        notificationService.markAsRead(userId, firstId);
        List<NotificationResponseDto> list3 = notificationService.getNotifications(userId);
        assertThat(list3.get(0).getUnread()).isFalse();
    }
}
