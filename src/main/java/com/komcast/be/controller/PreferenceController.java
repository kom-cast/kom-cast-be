package com.komcast.be.controller;

import com.komcast.be.dto.PreferencesResponseDto;
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
}
