package com.komcast.be.service;

import com.komcast.be.domain.Audio;
import com.komcast.be.domain.AudioSegment;
import com.komcast.be.domain.User;
import com.komcast.be.dto.BriefingItemDto;
import com.komcast.be.dto.BriefingResponseDto;
import com.komcast.be.dto.BriefingSegmentDto;
import com.komcast.be.repository.AudioRepository;
import com.komcast.be.repository.AudioSegmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BriefingService {

    private final AudioRepository audioRepository;
    private final AudioSegmentRepository audioSegmentRepository;
    private final PreferenceService preferenceService;

    @Transactional
    public BriefingResponseDto getTodayBriefing(Object userId) {
        User user = preferenceService.getOrCreateUser(userId);
        LocalDate today = LocalDate.now();

        Audio audio = audioRepository.findTopByUserIdAndAudioTypeOrderByCreatedAtDesc(user.getId(), "DAILY_BRIEFING")
                .orElseGet(() -> createDummyAudio(user));

        List<AudioSegment> segments = audioSegmentRepository.findByAudioIdOrderBySegmentOrderAsc(audio.getId());

        List<BriefingSegmentDto> segmentDtos = segments.stream()
                .map(this::mapToSegmentDto)
                .collect(Collectors.toList());

        return BriefingResponseDto.builder()
                .id(audio.getId())
                .date(today.toString())
                .headline("삼성전자·SK하이닉스 실적 서프라이즈")
                .audioUrl(audio.getAudioUrl())
                .durationSeconds(audio.getDurationSeconds())
                .segments(segmentDtos)
                .build();
    }

    public Page<BriefingItemDto> getBriefings(Object userId, Pageable pageable) {
        User user = preferenceService.getOrCreateUser(userId);
        Audio audio = audioRepository.findTopByUserIdAndAudioTypeOrderByCreatedAtDesc(user.getId(), "DAILY_BRIEFING")
                .orElseGet(() -> createDummyAudio(user));

        BriefingItemDto item = BriefingItemDto.builder()
                .id(audio.getId())
                .date(LocalDate.now().toString().replace("-", "."))
                .headline("삼성전자·SK하이닉스 실적 서프라이즈")
                .duration(String.valueOf(audio.getDurationSeconds() / 60))
                .build();

        return new PageImpl<>(List.of(item), pageable, 1);
    }

    public BriefingResponseDto getBriefingById(Object briefingId) {
        UUID aId = briefingId instanceof UUID ? (UUID) briefingId : UUID.fromString(briefingId.toString());
        Audio audio = audioRepository.findById(aId)
                .orElseThrow(() -> new IllegalArgumentException("Audio briefing not found with id: " + briefingId));

        List<AudioSegment> segments = audioSegmentRepository.findByAudioIdOrderBySegmentOrderAsc(audio.getId());

        List<BriefingSegmentDto> segmentDtos = segments.stream()
                .map(this::mapToSegmentDto)
                .collect(Collectors.toList());

        return BriefingResponseDto.builder()
                .id(audio.getId())
                .date(LocalDate.now().toString())
                .headline("삼성전자·SK하이닉스 실적 서프라이즈")
                .audioUrl(audio.getAudioUrl())
                .durationSeconds(audio.getDurationSeconds())
                .segments(segmentDtos)
                .build();
    }

    private BriefingSegmentDto mapToSegmentDto(AudioSegment s) {
        String code = s.getStockCode();
        String type = "USER";
        String stockCode = null;
        String industryCode = null;

        if (code != null && !code.isEmpty()) {
            if (code.startsWith("IND")) {
                type = "INDUSTRY";
                industryCode = code;
            } else {
                type = "STOCK";
                stockCode = code;
            }
        }

        return BriefingSegmentDto.builder()
                .speaker(s.getSpeaker())
                .target(BriefingSegmentDto.BriefingTargetDto.builder()
                        .type(type)
                        .stockCode(stockCode)
                        .industryCode(industryCode)
                        .build())
                .text(s.getText())
                .startSec(s.getStartSec())
                .words(List.of())
                .build();
    }

    @Transactional
    public Audio createDummyAudio(User user) {
        Audio audio = audioRepository.save(Audio.builder()
                .user(user)
                .audioType("DAILY_BRIEFING")
                .audioUrl("https://komcast-storage.ncp.com/audio/sample_briefing.mp3")
                .durationSeconds(600)
                .build());

        audioSegmentRepository.save(AudioSegment.builder()
                .audio(audio)
                .segmentOrder(1)
                .speaker("코스")
                .stockCode("005930")
                .text("삼성전자 관련 주요 소식으로 오늘의 브리핑을 시작합니다.")
                .startSec(0.0)
                .build());

        audioSegmentRepository.save(AudioSegment.builder()
                .audio(audio)
                .segmentOrder(2)
                .speaker("코미")
                .stockCode("005930")
                .text("삼성전자가 2분기 잠정 실적을 발표했습니다. 매출은 전년 동기 대비 23% 증가한 74조원을 기록했습니다.")
                .startSec(108.0)
                .build());

        audioSegmentRepository.save(AudioSegment.builder()
                .audio(audio)
                .segmentOrder(3)
                .speaker("코스")
                .stockCode("000660")
                .text("SK하이닉스는 메모리 가격 상승에 힘입어 목표주가가 상향 조정됐습니다.")
                .startSec(240.0)
                .build());

        return audio;
    }
}
