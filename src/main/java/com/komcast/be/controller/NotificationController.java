package com.komcast.be.controller;

import com.komcast.be.dto.ApiResponseDto;
import com.komcast.be.dto.NotificationResponseDto;
import com.komcast.be.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationResponseDto>> getNotifications(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        return ResponseEntity.ok(notificationService.getNotifications(userId));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponseDto> markAsRead(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @PathVariable("id") Long id) {
        notificationService.markAsRead(userId, id);
        return ResponseEntity.ok(ApiResponseDto.success("Notification " + id + " marked as read."));
    }

    @PostMapping("/read-all")
    public ResponseEntity<ApiResponseDto> markAllAsRead(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponseDto.success("All notifications marked as read."));
    }
}
