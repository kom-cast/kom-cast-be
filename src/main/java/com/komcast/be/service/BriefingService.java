package com.komcast.be.service;

import com.komcast.be.domain.Audio;
import com.komcast.be.domain.AudioSegment;
import com.komcast.be.domain.Notification;
import com.komcast.be.domain.User;
import com.komcast.be.dto.*;
import com.komcast.be.repository.AudioRepository;
import com.komcast.be.repository.AudioSegmentRepository;
import com.komcast.be.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BriefingService {

    private final AudioRepository audioRepository;
    private final AudioSegmentRepository audioSegmentRepository;
    private final NotificationRepository notificationRepository;
    private final PreferenceService preferenceService;
    private final AiClientService aiClientService;
    private final ScriptService scriptService;

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

    @Transactional
    public BriefingResponseDto generateBriefingByScriptId(Object userId, UUID scriptId) {
        User user = preferenceService.getOrCreateUser(userId);

        log.info("[Briefing Service] Requesting TTS generation for scriptId={} and user={}", scriptId, user.getId());

        TtsRequestDto ttsPayload = scriptService.getTtsPayloadFromScript(scriptId);
        TtsResponseDto ttsResponse = aiClientService.requestTtsGeneration(ttsPayload);

        if (ttsResponse != null) {
            String audioUrl = ttsResponse.getAudioUrl() != null ? ttsResponse.getAudioUrl() : "";
            int durationSec = ttsResponse.getDurationSec() != null ? ttsResponse.getDurationSec().intValue() : 0;

            Audio audio = audioRepository.save(Audio.builder()
                    .user(user)
                    .audioType("DAILY_BRIEFING")
                    .audioUrl(audioUrl)
                    .durationSeconds(durationSec)
                    .build());

            if (ttsResponse.getSegments() != null) {
                int order = 1;
                for (TtsResponseDto.TtsSegmentItem item : ttsResponse.getSegments()) {
                    String targetCode = item.getTarget() != null ? item.getTarget().getTargetCode() : null;
                    audioSegmentRepository.save(AudioSegment.builder()
                            .audio(audio)
                            .segmentOrder(order++)
                            .speaker(item.getSpeaker() != null ? item.getSpeaker() : "코스")
                            .stockCode(targetCode)
                            .text(item.getText() != null ? item.getText() : "")
                            .startSec(item.getStartSec() != null ? item.getStartSec() : 0.0)
                            .build());
                }
            }

            notificationRepository.save(Notification.builder()
                    .user(user)
                    .type("BRIEFING")
                    .title("오늘의 브리핑이 준비됐어요")
                    .description("새로운 맞춤형 아침 브리핑을 들어보세요")
                    .isRead(false)
                    .build());
        }

        return getTodayBriefing(userId);
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
