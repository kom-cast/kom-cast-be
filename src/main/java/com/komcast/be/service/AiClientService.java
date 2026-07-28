package com.komcast.be.service;

import com.komcast.be.dto.AiScriptRequestDto;
import com.komcast.be.dto.AiScriptResponseDto;
import com.komcast.be.dto.TtsResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiClientService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${external.ai-server-url:http://localhost:8000}")
    private String aiServerUrl;

    public AiScriptResponseDto requestScriptGeneration(AiScriptRequestDto requestDto) {
        String url = aiServerUrl + "/scripts/generate";
        log.info("[AI Client] Sending HTTP POST request to AI server: url={}, userCount={}", url, requestDto.getUserIds() != null ? requestDto.getUserIds().size() : 0);
        try {
            AiScriptResponseDto response = restTemplate.postForObject(url, requestDto, AiScriptResponseDto.class);
            log.info("[AI Client] Received response from AI server: response={}", response);
            return response;
        } catch (Exception e) {
            log.warn("[AI Client] Could not connect to AI server at {} ({})", url, e.getMessage());
            return null;
        }
    }

    public TtsResponseDto requestTtsGeneration(Object scriptPayload) {
        String url = aiServerUrl + "/briefings";
        log.info("[AI Client] Sending HTTP POST request for TTS generation: url={}", url);
        try {
            TtsResponseDto response = restTemplate.postForObject(url, scriptPayload, TtsResponseDto.class);
            log.info("[AI Client] Received TTS response from AI server: audioUrl={}", response != null ? response.getAudioUrl() : null);
            return response;
        } catch (Exception e) {
            log.warn("[AI Client] Could not connect to TTS service at {} ({})", url, e.getMessage());
            return null;
        }
    }
}
