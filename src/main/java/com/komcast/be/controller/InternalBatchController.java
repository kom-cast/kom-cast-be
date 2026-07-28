package com.komcast.be.controller;

import com.komcast.be.dto.BatchCompletionRequestDto;
import com.komcast.be.dto.TtsRequestDto;
import com.komcast.be.dto.TtsResponseDto;
import com.komcast.be.service.AiClientService;
import com.komcast.be.service.InternalBatchService;
import com.komcast.be.service.ScriptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "00. Internal Batch", description = "데이터/AI 서버 연동 비동기 배치 및 TTS 트리거 API")
@RestController
@RequestMapping("/api/v1/internal")
@RequiredArgsConstructor
public class InternalBatchController {

    private final InternalBatchService internalBatchService;
    private final AiClientService aiClientService;
    private final ScriptService scriptService;

    @Operation(summary = "데이터 정제 완료 트리거 수신 (비동기 처리)", description = "kom-cast-data 서버로부터 매일 배치 수집 완료 통지(POST 요청)를 받아 비동기적으로 AI 스크립트 생성을 요청합니다.")
    @PostMapping("/batch-complete")
    public ResponseEntity<Void> handleBatchComplete(@RequestBody(required = false) BatchCompletionRequestDto dto) {
        internalBatchService.processBatchCompletionAsync(dto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "AI TTS 직접 요청 단독 테스트 API", description = "TtsRequestDto payload를 전달받아 AI 서버의 POST /briefings (TTS 합성 엔진)을 직접 호출하고 결과를 반환합니다.")
    @PostMapping("/tts")
    public ResponseEntity<TtsResponseDto> synthesizeTtsDirect(@RequestBody TtsRequestDto ttsRequestDto) {
        TtsResponseDto ttsResponse = aiClientService.requestTtsGeneration(ttsRequestDto);
        return ResponseEntity.ok(ttsResponse);
    }

    @Operation(summary = "DB Script ID 기준 AI TTS 호출 API", description = "DB에 저장된 scriptId를 조회하여 실제 대본(sections, lines)을 조립한 뒤 AI TTS 엔진을 호출합니다.")
    @PostMapping("/scripts/{scriptId}/tts")
    public ResponseEntity<TtsResponseDto> synthesizeTtsByScriptId(@PathVariable("scriptId") UUID scriptId) {
        TtsRequestDto ttsPayload = scriptService.getTtsPayloadFromScript(scriptId);
        TtsResponseDto ttsResponse = aiClientService.requestTtsGeneration(ttsPayload);
        return ResponseEntity.ok(ttsResponse);
    }
}
