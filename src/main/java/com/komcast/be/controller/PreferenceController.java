package com.komcast.be.controller;

import com.komcast.be.dto.*;
import com.komcast.be.service.PreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "01. Preferences", description = "온보딩 및 사용자 환경설정 API")
@RestController
@RequestMapping("/api/v1/preferences")
@RequiredArgsConstructor
public class PreferenceController {

    private final PreferenceService preferenceService;

    @Operation(summary = "환경설정 종합 조회", description = "현재 사용자의 온보딩 설정, 보유종목, 관심분야, 키워드 및 알림 설정을 종합 조회합니다.")
    @GetMapping
    public ResponseEntity<PreferencesResponseDto> getPreferences(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        return ResponseEntity.ok(preferenceService.getPreferences(userId));
    }

    @Operation(summary = "환경설정 전체 수정", description = "온보딩 완료 시 또는 마이페이지 설정 변경 시 전체 설정을 일괄 저장합니다.")
    @PutMapping
    public ResponseEntity<ApiResponseDto> updatePreferences(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @RequestBody PreferencesUpdateRequestDto dto) {
        preferenceService.updatePreferences(userId, dto);
        return ResponseEntity.ok(ApiResponseDto.success("Preferences updated successfully."));
    }

    @Operation(summary = "브리핑 목소리 단건 변경", description = "TTS 브리핑 성우 목소리를 변경합니다.")
    @PatchMapping("/voice")
    public ResponseEntity<ApiResponseDto> updateVoice(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @RequestBody VoiceUpdateRequestDto dto) {
        preferenceService.updateVoice(userId, dto.getVoice());
        return ResponseEntity.ok(ApiResponseDto.success("Voice set to " + dto.getVoice() + "."));
    }

    @Operation(summary = "브리핑 재생 분량 변경", description = "브리핑 재생 분량(5, 10, 15분)을 변경합니다.")
    @PatchMapping("/briefing-duration")
    public ResponseEntity<ApiResponseDto> updateDuration(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @RequestBody DurationUpdateRequestDto dto) {
        preferenceService.updateDuration(userId, dto.getBriefingDuration());
        return ResponseEntity.ok(ApiResponseDto.success("Briefing duration updated to " + dto.getBriefingDuration() + " minutes."));
    }

    @Operation(summary = "푸시 알림 수신 토글", description = "브리핑, 시세, 마케팅 알림의 개별 수신 동의 여부를 수정합니다.")
    @PatchMapping("/notifications")
    public ResponseEntity<ApiResponseDto> updateNotifications(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @RequestBody NotificationToggleRequestDto dto) {
        preferenceService.updateNotifications(userId, dto);
        return ResponseEntity.ok(ApiResponseDto.success("Notification preferences updated."));
    }
}
