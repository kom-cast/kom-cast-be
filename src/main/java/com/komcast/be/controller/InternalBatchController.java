package com.komcast.be.controller;

import com.komcast.be.dto.BatchCompletionRequestDto;
import com.komcast.be.service.InternalBatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "00. Internal Batch", description = "데이터/AI 서버 연동 비동기 배치 트리거 API")
@RestController
@RequestMapping("/api/v1/internal")
@RequiredArgsConstructor
public class InternalBatchController {

    private final InternalBatchService internalBatchService;

    @Operation(summary = "데이터 정제 완료 트리거 수신 (비동기 처리)", description = "kom-cast-data 서버로부터 매일 배치 수집 완료 통지(POST 요청)를 받아 비동기적으로 AI 스크립트 생성을 요청합니다. (Request & Response Body 모두 선택 사항)")
    @PostMapping("/batch-complete")
    public ResponseEntity<Void> handleBatchComplete(@RequestBody(required = false) BatchCompletionRequestDto dto) {
        internalBatchService.processBatchCompletionAsync(dto);
        return ResponseEntity.ok().build();
    }
}
