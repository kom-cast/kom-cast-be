package com.komcast.be.controller;

import com.komcast.be.dto.*;
import com.komcast.be.service.PreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/preferences")
@RequiredArgsConstructor
public class PreferenceController {

    private final PreferenceService preferenceService;

    @GetMapping
    public ResponseEntity<PreferencesResponseDto> getPreferences(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        return ResponseEntity.ok(preferenceService.getPreferences(userId));
    }

    @PutMapping
    public ResponseEntity<ApiResponseDto> updatePreferences(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @RequestBody PreferencesUpdateRequestDto dto) {
        preferenceService.updatePreferences(userId, dto);
        return ResponseEntity.ok(ApiResponseDto.success("Preferences updated successfully."));
    }

    @PatchMapping("/voice")
    public ResponseEntity<ApiResponseDto> updateVoice(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @RequestBody VoiceUpdateRequestDto dto) {
        preferenceService.updateVoice(userId, dto.getVoice());
        return ResponseEntity.ok(ApiResponseDto.success("Voice set to " + dto.getVoice() + "."));
    }

    @PatchMapping("/briefing-duration")
    public ResponseEntity<ApiResponseDto> updateDuration(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @RequestBody DurationUpdateRequestDto dto) {
        preferenceService.updateDuration(userId, dto.getBriefingDuration());
        return ResponseEntity.ok(ApiResponseDto.success("Briefing duration updated to " + dto.getBriefingDuration() + " minutes."));
    }

    @PatchMapping("/notifications")
    public ResponseEntity<ApiResponseDto> updateNotifications(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @RequestBody NotificationToggleRequestDto dto) {
        preferenceService.updateNotifications(userId, dto);
        return ResponseEntity.ok(ApiResponseDto.success("Notification preferences updated."));
    }
}
