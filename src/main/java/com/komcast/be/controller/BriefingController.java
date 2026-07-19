package com.komcast.be.controller;

import com.komcast.be.dto.BriefingItemDto;
import com.komcast.be.dto.BriefingResponseDto;
import com.komcast.be.service.BriefingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/briefings")
@RequiredArgsConstructor
public class BriefingController {

    private final BriefingService briefingService;

    @GetMapping("/today")
    public ResponseEntity<BriefingResponseDto> getTodayBriefing(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        return ResponseEntity.ok(briefingService.getTodayBriefing(userId));
    }

    @GetMapping
    public ResponseEntity<Page<BriefingItemDto>> getBriefings(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(briefingService.getBriefings(userId, PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BriefingResponseDto> getBriefingById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(briefingService.getBriefingById(id));
    }
}
