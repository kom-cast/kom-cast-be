package com.komcast.be.controller;

import com.komcast.be.dto.BriefingItemDto;
import com.komcast.be.dto.BriefingResponseDto;
import com.komcast.be.service.BriefingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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

    @Operation(summary = "스크립트 ID 기준 단독 맞춤 브리핑 생성 (Path Variable)", description = "지정한 scriptId를 DB에서 조회해 대본을 조립한 후 TTS 음성 합성을 즉시 실행하고 완성된 브리핑을 반환합니다.")
    @PostMapping("/generate/{scriptId}")
    public ResponseEntity<BriefingResponseDto> generateBriefingByScriptIdPath(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") String userId,
            @PathVariable("scriptId") UUID scriptId) {
        return ResponseEntity.ok(briefingService.generateBriefingByScriptId(userId, scriptId));
    }

    @Operation(summary = "스크립트 ID 기준 단독 맞춤 브리핑 생성 (Query Param)", description = "지정한 scriptId를 쿼리 파라미터로 넘겨 DB 대본 조립 -> TTS 음성 합성을 즉시 실행하고 완성된 브리핑을 반환합니다.")
    @PostMapping("/generate")
    public ResponseEntity<BriefingResponseDto> generateBriefingByScriptIdParam(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") String userId,
            @RequestParam(name = "scriptId") UUID scriptId) {
        return ResponseEntity.ok(briefingService.generateBriefingByScriptId(userId, scriptId));
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

    @Operation(summary = "브리핑 오디오 바이너리 다운로드", description = "audio_binaries 테이블에 저장된 MP3 바이너리를 스트리밍으로 반환합니다. (Object Storage 임시 대안)")
    @GetMapping("/{id}/audio")
    public ResponseEntity<ResourceRegion> getAudioBinary(
            @PathVariable("id") UUID id,
            @RequestHeader HttpHeaders headers) {
        byte[] data = briefingService.getAudioBinary(id);
        ByteArrayResource resource = new ByteArrayResource(data);
        long contentLength = data.length;

        // NOTE: 프론트에서 오디오 탐색(seek)이 되려면 브라우저가 Range 요청으로
        // 특정 구간만 다시 요청했을 때 206 Partial Content + Content-Range로
        // 응답해줘야 함. byte[]를 그냥 200으로 통째로 내려주면 브라우저가
        // Accept-Ranges를 못 받아 seekable 구간을 0으로 인식하고, currentTime을
        // 옮겨도 재생이 0초로 리셋됨.
        List<HttpRange> ranges = headers.getRange();
        HttpStatus status;
        ResourceRegion region;
        if (ranges.isEmpty()) {
            status = HttpStatus.OK;
            region = new ResourceRegion(resource, 0, contentLength);
        } else {
            status = HttpStatus.PARTIAL_CONTENT;
            HttpRange range = ranges.get(0);
            long start = range.getRangeStart(contentLength);
            long end = range.getRangeEnd(contentLength);
            region = new ResourceRegion(resource, start, end - start + 1);
        }

        return ResponseEntity.status(status)
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"briefing.mp3\"")
                .body(region);
    }
}
