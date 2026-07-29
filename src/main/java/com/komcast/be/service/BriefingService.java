package com.komcast.be.service;

import com.komcast.be.domain.Audio;
import com.komcast.be.domain.AudioBinary;
import com.komcast.be.domain.AudioSegment;
import com.komcast.be.domain.Notification;
import com.komcast.be.domain.User;
import com.komcast.be.dto.*;
import com.komcast.be.repository.AudioBinaryRepository;
import com.komcast.be.repository.AudioRepository;
import com.komcast.be.repository.AudioSegmentRepository;
import com.komcast.be.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
    private final AudioBinaryRepository audioBinaryRepository;
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

        // audio_url이 UUID 형태면 /audio 다운로드 URL로 변환, 아니면 그대로 사용
        String audioUrl = resolveAudioUrl(audio);

        return BriefingResponseDto.builder()
                .id(audio.getId())
                .date(today.toString())
                .headline("오늘의 AI 개인화 맞춤 브리핑")
                .audioUrl(audioUrl)
                .durationSeconds(audio.getDurationSeconds())
                .segments(segmentDtos)
                .build();
    }

    @Transactional
    public BriefingResponseDto generateBriefingByScriptId(Object userId, UUID scriptId) {
        User user = preferenceService.getOrCreateUser(userId);

        log.info("[Briefing Service] Requesting TTS generation for scriptId={} and user={}", scriptId, user.getId());

        TtsRequestDto ttsPayload = scriptService.getTtsPayloadFromScript(scriptId, user.getId());
        TtsResponseDto ttsResponse = aiClientService.requestTtsGeneration(ttsPayload);

        // audio_binary_id가 있으면 UUID를 audio_url로 저장, 없으면 audioUrl 저장
        String audioUrlToSave = "";
        if (ttsResponse.getAudioBinaryId() != null && !ttsResponse.getAudioBinaryId().isBlank()) {
            audioUrlToSave = ttsResponse.getAudioBinaryId();
            log.info("[Briefing Service] audio_binary_id received: {}", audioUrlToSave);
        } else if (ttsResponse.getAudioUrl() != null && !ttsResponse.getAudioUrl().isBlank()) {
            audioUrlToSave = ttsResponse.getAudioUrl();
            log.info("[Briefing Service] audioUrl received: {}", audioUrlToSave);
        }

        int durationSec = ttsResponse.getDurationSec() != null ? ttsResponse.getDurationSec().intValue() : 0;

        Audio audio = audioRepository.save(Audio.builder()
                .user(user)
                .audioType("DAILY_BRIEFING")
                .audioUrl(audioUrlToSave)
                .durationSeconds(durationSec)
                .build());


        notificationRepository.save(Notification.builder()
                .user(user)
                .type("BRIEFING")
                .title("오늘의 브리핑이 준비됐어요")
                .description("새로운 맞춤형 아침 브리핑을 들어보세요")
                .isRead(false)
                .build());

        return getTodayBriefing(userId);
    }

    public byte[] getAudioBinary(UUID audioId) {
        Audio audio = audioRepository.findById(audioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "브리핑을 찾을 수 없습니다: " + audioId));

        String audioUrl = audio.getAudioUrl();
        if (audioUrl == null || audioUrl.isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "오디오 바이너리가 없습니다.");
        }

        try {
            UUID binaryId = UUID.fromString(audioUrl);
            AudioBinary binary = audioBinaryRepository.findById(binaryId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "오디오 바이너리를 찾을 수 없습니다: " + binaryId));
            return binary.getData();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "오디오 바이너리가 DB에 저장된 형태가 아닙니다. (audioUrl=" + audioUrl + ")");
        }
    }

    public Page<BriefingItemDto> getBriefings(Object userId, Pageable pageable) {
        User user = preferenceService.getOrCreateUser(userId);
        
        Page<Audio> audioPage = audioRepository.findByUserIdAndAudioTypeOrderByCreatedAtDesc(user.getId(), "DAILY_BRIEFING", pageable);

        return audioPage.map(audio -> BriefingItemDto.builder()
                .id(audio.getId())
                .date(audio.getCreatedAt() != null ? audio.getCreatedAt().toLocalDate().toString() : LocalDate.now().toString())
                .headline("오늘의 AI 개인화 맞춤 브리핑")
                .duration(String.valueOf(audio.getDurationSeconds() / 60))
                .build());
    }

    public BriefingResponseDto getBriefingById(Object briefingId) {
        UUID aId = briefingId instanceof UUID ? (UUID) briefingId : UUID.fromString(briefingId.toString());
        Audio audio = audioRepository.findById(aId)
                .orElseThrow(() -> new IllegalArgumentException("Audio briefing not found with id: " + briefingId));

        List<AudioSegment> segments = audioSegmentRepository.findByAudioIdOrderBySegmentOrderAsc(audio.getId());

        List<BriefingSegmentDto> segmentDtos = segments.stream()
                .map(s -> mapToSegmentDto(s, audio.getDurationSeconds()))
                .collect(Collectors.toList());

        String audioUrl = resolveAudioUrl(audio);

        return BriefingResponseDto.builder()
                .id(audio.getId())
                .date(audio.getCreatedAt() != null ? audio.getCreatedAt().toLocalDate().toString() : LocalDate.now().toString())
                .headline("오늘의 AI 개인화 맞춤 브리핑")
                .audioUrl(audioUrl)
                .durationSeconds(audio.getDurationSeconds())
                .segments(segmentDtos)
                .build();
    }

    /**
     * audio_url이 UUID 형태이면 → 바이너리 다운로드 API URL로 변환
     * 일반 URL이면 → 그대로 반환
     */
    private String resolveAudioUrl(Audio audio) {
        String rawUrl = audio.getAudioUrl();
        if (rawUrl == null || rawUrl.isBlank()) return "";
        try {
            UUID.fromString(rawUrl);
            return "/api/v1/briefings/" + audio.getId() + "/audio";
        } catch (IllegalArgumentException e) {
            return rawUrl;
        }
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
