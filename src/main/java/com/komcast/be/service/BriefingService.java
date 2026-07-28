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
import java.util.ArrayList;
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
                .orElse(null);

        if (audio == null) {
            return BriefingResponseDto.builder()
                    .id(null)
                    .date(today.toString())
                    .headline("오늘의 브리핑이 준비 중입니다.")
                    .audioUrl("")
                    .durationSeconds(0)
                    .segments(List.of())
                    .build();
        }

        List<AudioSegment> segments = audioSegmentRepository.findByAudioIdOrderBySegmentOrderAsc(audio.getId());

        List<BriefingSegmentDto> segmentDtos = segments.stream()
                .map(s -> mapToSegmentDto(s, audio.getDurationSeconds()))
                .collect(Collectors.toList());

        return BriefingResponseDto.builder()
                .id(audio.getId())
                .date(today.toString())
                .headline("오늘의 AI 개인화 맞춤 브리핑")
                .audioUrl(audio.getAudioUrl())
                .durationSeconds(audio.getDurationSeconds())
                .segments(segmentDtos)
                .build();
    }

    public Page<BriefingItemDto> getBriefings(Object userId, Pageable pageable) {
        User user = preferenceService.getOrCreateUser(userId);
        Audio audio = audioRepository.findTopByUserIdAndAudioTypeOrderByCreatedAtDesc(user.getId(), "DAILY_BRIEFING")
                .orElse(null);

        if (audio == null) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        BriefingItemDto item = BriefingItemDto.builder()
                .id(audio.getId())
                .date(LocalDate.now().toString())
                .headline("오늘의 AI 개인화 맞춤 브리핑")
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
                .map(s -> mapToSegmentDto(s, audio.getDurationSeconds()))
                .collect(Collectors.toList());

        return BriefingResponseDto.builder()
                .id(audio.getId())
                .date(LocalDate.now().toString())
                .headline("오늘의 AI 개인화 맞춤 브리핑")
                .audioUrl(audio.getAudioUrl())
                .durationSeconds(audio.getDurationSeconds())
                .segments(segmentDtos)
                .build();
    }

    private BriefingSegmentDto mapToSegmentDto(AudioSegment s, Integer totalDuration) {
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

        double startSec = (s.getStartSec() != null) ? s.getStartSec() : 0.0;
        double duration = (totalDuration != null && totalDuration > 0) ? (double) totalDuration : 600.0;
        double fraction = Math.round((startSec / duration) * 1000.0) / 1000.0;

        List<BriefingSegmentDto.WordTimestampDto> wordTimestamps = generateWordTimestamps(s.getText(), startSec);

        return BriefingSegmentDto.builder()
                .fraction(fraction)
                .speaker(s.getSpeaker())
                .target(BriefingSegmentDto.BriefingTargetDto.builder()
                        .type(type)
                        .stockCode(stockCode)
                        .industryCode(industryCode)
                        .build())
                .text(s.getText())
                .startSec(startSec)
                .words(wordTimestamps)
                .build();
    }

    private List<BriefingSegmentDto.WordTimestampDto> generateWordTimestamps(String text, double startSec) {
        if (text == null || text.trim().isEmpty()) {
            return List.of();
        }

        String[] tokens = text.trim().split("\\s+");
        List<BriefingSegmentDto.WordTimestampDto> words = new ArrayList<>();

        double current = startSec;
        for (String token : tokens) {
            double wordDuration = Math.max(0.3, Math.round(token.length() * 0.12 * 100.0) / 100.0);
            double end = Math.round((current + wordDuration) * 100.0) / 100.0;

            words.add(BriefingSegmentDto.WordTimestampDto.builder()
                    .text(token)
                    .startSec(Math.round(current * 100.0) / 100.0)
                    .endSec(end)
                    .build());

            current = end + 0.05;
        }

        return words;
    }
}
