package com.komcast.be.controller;

import com.komcast.be.dto.ApiResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "00. Health Check", description = "서버 상태 헬스 체크 API")
@RestController
public class HealthController {

    @Operation(summary = "서버 헬스 체크", description = "서버 정상 구동 여부를 확인합니다.")
    @GetMapping({"/health", "/api/v1/health"})
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "kom-cast-be",
                "message", "Server is running normally."
        ));
    }
}
