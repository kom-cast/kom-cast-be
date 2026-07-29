package com.komcast.be.service;

import com.komcast.be.domain.Audio;
import com.komcast.be.domain.AudioSegment;
import com.komcast.be.domain.Notification;
import com.komcast.be.domain.User;
import com.komcast.be.domain.UserPlan;
import com.komcast.be.dto.*;
import com.komcast.be.repository.AudioRepository;
import com.komcast.be.repository.AudioSegmentRepository;
import com.komcast.be.repository.NotificationRepository;
import com.komcast.be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InternalBatchService {

    private final UserRepository userRepository;
    private final AiClientService aiClientService;
    private final ScriptService scriptService;
    private final AudioRepository audioRepository;
    private final AudioSegmentRepository audioSegmentRepository;
    private final NotificationRepository notificationRepository;

    @Async
    @Transactional
    public void processBatchCompletionAsync(BatchCompletionRequestDto dto) {
        LocalDate targetDate = (dto != null && dto.getRunDate() != null)
                ? LocalDate.parse(dto.getRunDate())
                : LocalDate.now().minusDays(1);

        log.info("[Async Batch] Received completion POST webhook from kom-cast-data for targetDate={}. Starting async processing...", targetDate);

        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            User defaultUser = userRepository.save(User.builder()
                    .nickname("민준")
                    .plan(UserPlan.STANDARD)
                    .build());
            users = List.of(defaultUser);
        }

        String startAt = targetDate.toString() + "T00:00:00+09:00";
        String endAt = targetDate.plusDays(1).toString() + "T00:00:00+09:00";

        List<String> userIds = users.stream()
                .map(u -> String.valueOf(u.getId()))
                .toList();

        AiScriptRequestDto aiRequest = AiScriptRequestDto.builder()
                .startAt(startAt)
                .endAt(endAt)
                .userIds(userIds)
                .build();

        log.info("[Async Batch] Step 1: Requesting AI script generation for targetDate={}", targetDate);
        AiScriptResponseDto scriptResponse;
        try {
            scriptResponse = aiClientService.requestScriptGeneration(aiRequest);
        } catch (Exception e) {
            log.error("[Async Batch] AI 스크립트 생성 요청 실패, 배치 중단: {}", e.getMessage());
            return;
        }

        if (scriptResponse.getScripts() == null || scriptResponse.getScripts().isEmpty()) {
            log.warn("[Async Batch] No scripts returned from AI server for targetDate={}", targetDate);
            return;
        }

        for (AiScriptResponseDto.GeneratedScriptItem scriptItem : scriptResponse.getScripts()) {
            if (scriptItem.getScriptId() == null || scriptItem.getUserId() == null) {
                continue;
            }

            try {
                UUID scriptId = UUID.fromString(scriptItem.getScriptId());
                UUID userId = UUID.fromString(scriptItem.getUserId());

                User user = userRepository.findById(userId).orElse(null);
                if (user == null) {
                    continue;
                }

                log.info("[Async Batch] Step 2: Querying script from DB for scriptId={} and generating TTS", scriptId);
                TtsRequestDto ttsPayload = scriptService.getTtsPayloadFromScript(scriptId, userId);

                TtsResponseDto ttsResponse = aiClientService.requestTtsGeneration(ttsPayload);

                String audioUrl = ttsResponse.getAudioUrl() != null ? ttsResponse.getAudioUrl() : "";
                // 백엔드에서의 중복 오디오 저장을 제거 (인공지능 서버에서 저장함)
                // 백엔드에서의 중복 오디오 및 세그먼트 저장을 제거 (인공지능 서버에서 모두 저장함)

                notificationRepository.save(Notification.builder()
                        .user(user)
                        .type("BRIEFING")
                        .title("오늘의 브리핑이 준비됐어요")
                        .description("새로운 맞춤형 아침 브리핑을 들어보세요")
                        .isRead(false)
                        .build());

                log.info("[Async Batch] Audio & Notification successfully saved for userId={}", userId);

            } catch (Exception e) {
                log.error("[Async Batch] Error processing scriptId={}: {}", scriptItem.getScriptId(), e.getMessage(), e);
            }
        }

        log.info("[Async Batch] All async AI script, TTS, DB persistence, and notification dispatches finished for targetDate={}", targetDate);
    }
}
