package com.komcast.be.service;

import com.komcast.be.domain.Briefing;
import com.komcast.be.domain.BriefingSegment;
import com.komcast.be.domain.User;
import com.komcast.be.dto.BriefingItemDto;
import com.komcast.be.dto.BriefingResponseDto;
import com.komcast.be.dto.BriefingSegmentDto;
import com.komcast.be.repository.BriefingRepository;
import com.komcast.be.repository.BriefingSegmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BriefingService {

    private final BriefingRepository briefingRepository;
    private final BriefingSegmentRepository briefingSegmentRepository;
    private final PreferenceService preferenceService;

    @Transactional
    public BriefingResponseDto getTodayBriefing(Long userId) {
        User user = preferenceService.getOrCreateUser(userId);
        LocalDate today = LocalDate.now();

        Briefing briefing = briefingRepository.findTopByUserIdAndDateOrderByCreatedAtDesc(user.getId(), today)
                .orElseGet(() -> createDummyBriefing(user, today));

        List<BriefingSegment> segments = briefingSegmentRepository.findByBriefingIdOrderByFractionAsc(briefing.getId());

        List<BriefingSegmentDto> segmentDtos = segments.stream()
                .map(s -> BriefingSegmentDto.builder()
                        .fraction(s.getFraction())
                        .stock(s.getStockName())
                        .text(s.getText())
                        .build())
                .collect(Collectors.toList());

        return BriefingResponseDto.builder()
                .id(briefing.getId())
                .date(briefing.getDate().toString())
                .headline(briefing.getHeadline())
                .audioUrl(briefing.getAudioUrl())
                .durationSeconds(briefing.getDurationSeconds())
                .segments(segmentDtos)
                .build();
    }

    public Page<BriefingItemDto> getBriefings(Long userId, Pageable pageable) {
        User user = preferenceService.getOrCreateUser(userId);
        Page<Briefing> page = briefingRepository.findByUserIdOrderByDateDesc(user.getId(), pageable);

        if (page.isEmpty()) {
            // 더미 1건 생성 후 재조회
            createDummyBriefing(user, LocalDate.now());
            page = briefingRepository.findByUserIdOrderByDateDesc(user.getId(), pageable);
        }

        return page.map(b -> BriefingItemDto.builder()
                .id(b.getId())
                .date(b.getDate().toString().replace("-", "."))
                .headline(b.getHeadline())
                .duration(String.valueOf(b.getDurationSeconds() / 60))
                .build());
    }

    public BriefingResponseDto getBriefingById(Long briefingId) {
        Briefing briefing = briefingRepository.findById(briefingId)
                .orElseThrow(() -> new IllegalArgumentException("Briefing not found with id: " + briefingId));

        List<BriefingSegment> segments = briefingSegmentRepository.findByBriefingIdOrderByFractionAsc(briefing.getId());

        List<BriefingSegmentDto> segmentDtos = segments.stream()
                .map(s -> BriefingSegmentDto.builder()
                        .fraction(s.getFraction())
                        .stock(s.getStockName())
                        .text(s.getText())
                        .build())
                .collect(Collectors.toList());

        return BriefingResponseDto.builder()
                .id(briefing.getId())
                .date(briefing.getDate().toString())
                .headline(briefing.getHeadline())
                .audioUrl(briefing.getAudioUrl())
                .durationSeconds(briefing.getDurationSeconds())
                .segments(segmentDtos)
                .build();
    }

    @Transactional
    public Briefing createDummyBriefing(User user, LocalDate date) {
        Briefing briefing = Briefing.builder()
                .user(user)
                .date(date)
                .headline("삼성전자·SK하이닉스 실적 서프라이즈")
                .audioUrl("https://komcast-storage.ncp.com/audio/sample_briefing.mp3")
                .durationSeconds(600)
                .build();

        briefing.addSegment(BriefingSegment.builder()
                .fraction(0.0)
                .stockName("삼성전자")
                .text("삼성전자 관련 주요 소식으로 오늘의 브리핑을 시작합니다.")
                .build());

        briefing.addSegment(BriefingSegment.builder()
                .fraction(0.18)
                .stockName("삼성전자")
                .text("삼성전자가 2분기 잠정 실적을 발표했습니다. 매출은 전년 동기 대비 23% 증가한 74조원을 기록했습니다.")
                .build());

        briefing.addSegment(BriefingSegment.builder()
                .fraction(0.4)
                .stockName("SK하이닉스")
                .text("SK하이닉스는 메모리 가격 상승에 힘입어 목표주가가 상향 조정됐습니다.")
                .build());

        briefing.addSegment(BriefingSegment.builder()
                .fraction(0.62)
                .stockName("2차전지")
                .text("2차전지 섹터 전반이 반등하며 관련주들이 강세를 보이고 있습니다.")
                .build());

        briefing.addSegment(BriefingSegment.builder()
                .fraction(0.85)
                .stockName("시장 전체")
                .text("시장 관련 주요 이슈를 마지막으로 오늘의 브리핑을 마칩니다.")
                .build());

        return briefingRepository.save(briefing);
    }
}
