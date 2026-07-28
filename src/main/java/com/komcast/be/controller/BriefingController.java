package com.komcast.be.controller;

import com.komcast.be.dto.BriefingItemDto;
import com.komcast.be.dto.BriefingResponseDto;
import com.komcast.be.service.BriefingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "03. Briefings", description = "AI 브리핑 오디오 및 대본 조회/생성/보관함 API")
@RestController
@RequestMapping("/api/v1/briefings")
@RequiredArgsConstructor
public class BriefingController {

    private final BriefingService briefingService;

    @Operation(summary = "오늘의 맞춤 브리핑 조회", description = "당일 사용자 맞춤 AI 브리핑 오디오 파일 URL 및 시간대별 자막 대본 세그먼트를 반환합니다.")
    @GetMapping("/today")
    public ResponseEntity<BriefingResponseDto> getTodayBriefing(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") String userId) {
        return ResponseEntity.ok(briefingService.getTodayBriefing(userId));
    }

    @Operation(summary = "단독 맞춤 브리핑 수동 생성 요청 (유저/시연용)", description = "특정 사용자에 대해 AI 스크립트 작성 -> DB 스크립트 쿼리 -> TTS 오디오 수동 합성을 즉시 실행하고 결과 브리핑을 반환합니다.")
    @PostMapping("/generate")
    public ResponseEntity<BriefingResponseDto> generateUserBriefing(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") String userId,
            @RequestParam(name = "runDate", required = false) String runDate) {
        return ResponseEntity.ok(briefingService.generateUserBriefing(userId, runDate));
    }

    @Operation(summary = "과거 브리핑 보관함 이력 조회 (페이징)", description = "보관함 페이지에서 과거 생성된 브리핑 이력들을 페이징 단위로 목록 조회합니다.")
    @GetMapping
    public ResponseEntity<Page<BriefingItemDto>> getBriefings(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") String userId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(briefingService.getBriefings(userId, PageRequest.of(page, size)));
    }

    @Operation(summary = "특정 과거 브리핑 상세 조회", description = "보관함 카드 클릭 시 선택한 브리핑의 오디오 URL 및 자막 세그먼트를 재조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<BriefingResponseDto> getBriefingById(@PathVariable("id") String id) {
        return ResponseEntity.ok(briefingService.getBriefingById(id));
    }
}
