package com.komcast.be.controller;

import com.komcast.be.dto.ApiResponseDto;
import com.komcast.be.dto.StockRegisterRequestDto;
import com.komcast.be.dto.StockResponseDto;
import com.komcast.be.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @GetMapping("/stocks")
    public ResponseEntity<List<StockResponseDto>> getAllStocks() {
        return ResponseEntity.ok(stockService.getAllStocks());
    }

    @GetMapping("/stocks/my")
    public ResponseEntity<List<StockResponseDto>> getMyStocks(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        return ResponseEntity.ok(stockService.getMyStocks(userId));
    }

    @PostMapping("/stocks/my")
    public ResponseEntity<ApiResponseDto> registerMyStock(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @RequestBody StockRegisterRequestDto dto) {
        stockService.registerMyStock(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Stock " + dto.getCode() + " registered to portfolio."));
    }

    @DeleteMapping("/stocks/my/{code}")
    public ResponseEntity<ApiResponseDto> deleteMyStock(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @PathVariable("code") String code) {
        stockService.deleteMyStock(userId, code);
        return ResponseEntity.ok(ApiResponseDto.success("Stock " + code + " removed from portfolio."));
    }

    @GetMapping("/sectors")
    public ResponseEntity<List<String>> getAllSectors() {
        return ResponseEntity.ok(stockService.getAllSectors());
    }
}
