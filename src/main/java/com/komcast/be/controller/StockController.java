package com.komcast.be.controller;

import com.komcast.be.dto.ApiResponseDto;
import com.komcast.be.dto.IndustryResponseDto;
import com.komcast.be.dto.StockRegisterRequestDto;
import com.komcast.be.dto.StockResponseDto;
import com.komcast.be.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "02. Stocks & Industries", description = "종목 및 산업 분야 관리 API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @Operation(summary = "전체 주식 마스터 리스트 조회", description = "종목 추가/검색용 전체 마스터 주식 데이터 리스트를 반환합니다.")
    @GetMapping("/stocks")
    public ResponseEntity<List<StockResponseDto>> getAllStocks() {
        return ResponseEntity.ok(stockService.getAllStocks());
    }

    @Operation(summary = "나의 보유/관심 종목 및 시세 조회", description = "사용자가 등록한 보유/관심 종목 목록 및 실시간 시세를 반환합니다.")
    @GetMapping("/stocks/my")
    public ResponseEntity<List<StockResponseDto>> getMyStocks(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        return ResponseEntity.ok(stockService.getMyStocks(userId));
    }

    @Operation(summary = "보유/관심 종목 추가 등록", description = "새로운 종목을 사용자의 보유/관심 목록에 추가합니다.")
    @PostMapping("/stocks/my")
    public ResponseEntity<ApiResponseDto> registerMyStock(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @RequestBody StockRegisterRequestDto dto) {
        stockService.registerMyStock(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Stock " + dto.getCode() + " registered to portfolio."));
    }

    @Operation(summary = "보유/관심 종목 삭제", description = "사용자의 목록에서 특정 종목을 해제합니다.")
    @DeleteMapping("/stocks/my/{code}")
    public ResponseEntity<ApiResponseDto> deleteMyStock(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @PathVariable("code") String code) {
        stockService.deleteMyStock(userId, code);
        return ResponseEntity.ok(ApiResponseDto.success("Stock " + code + " removed from portfolio."));
    }

    @Operation(summary = "전체 관심 산업 분야 목록 조회", description = "온보딩에서 선택 가능한 전체 관심 산업 분야 리스트를 반환합니다.")
    @GetMapping("/industries")
    public ResponseEntity<List<IndustryResponseDto>> getAllIndustries() {
        return ResponseEntity.ok(stockService.getAllIndustries());
    }
}
