package com.komcast.be.controller;

import com.komcast.be.dto.ApiResponseDto;
import com.komcast.be.dto.NotificationResponseDto;
import com.komcast.be.dto.NotificationToggleRequestDto;
import com.komcast.be.service.NotificationService;
import com.komcast.be.service.PreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "04. Notifications", description = "알림센터 푸시/이벤트 히스토리 및 알림 설정 API")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final PreferenceService preferenceService;

    @Operation(summary = "수신 알림 목록 조회", description = "사용자에게 수신된 브리핑/시세/시스템 알림 이력 목록을 반환합니다.")
    @GetMapping
    public ResponseEntity<List<NotificationResponseDto>> getNotifications(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        return ResponseEntity.ok(notificationService.getNotifications(userId));
    }

    @Operation(summary = "알림 설정 조회", description = "알림 수신 동의 상태를 조회합니다.")
    @GetMapping("/settings")
    public ResponseEntity<NotificationToggleRequestDto> getNotificationSettings(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        return ResponseEntity.ok(preferenceService.getNotificationSettings(userId));
    }

    @Operation(summary = "알림 설정 변경", description = "알림 수신 동의 상태를 수정합니다.")
    @PatchMapping("/settings")
    public ResponseEntity<ApiResponseDto> updateNotificationSettings(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @RequestBody NotificationToggleRequestDto dto) {
        preferenceService.updateNotifications(userId, dto);
        return ResponseEntity.ok(ApiResponseDto.success("Notification settings updated."));
    }

    @Operation(summary = "개별 알림 읽음 처리", description = "특정 알림 항목을 읽음(unread: false) 상태로 업데이트합니다.")
    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponseDto> markAsRead(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @PathVariable("id") Long id) {
        notificationService.markAsRead(userId, id);
        return ResponseEntity.ok(ApiResponseDto.success("Notification " + id + " marked as read."));
    }

    @Operation(summary = "모든 알림 일괄 읽음 처리", description = "수신된 모든 알림을 한 번에 읽음 상태로 업데이트합니다.")
    @PostMapping("/read-all")
    public ResponseEntity<ApiResponseDto> markAllAsRead(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponseDto.success("All notifications marked as read."));
    }
}
