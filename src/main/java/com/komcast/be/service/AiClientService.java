package com.komcast.be.service;

import com.komcast.be.dto.AiScriptRequestDto;
import com.komcast.be.dto.AiScriptResponseDto;
import com.komcast.be.dto.TtsResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiClientService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;

    @Value("${external.ai-server-url:http://localhost:8000}")
    private String aiServerUrl;

    public AiScriptResponseDto requestScriptGeneration(AiScriptRequestDto requestDto) {
        String url = aiServerUrl + "/scripts/generate";
        log.info("[AI Client] Sending HTTP POST request to AI server: url={}, userCount={}", url,
                requestDto.getUserIds() != null ? requestDto.getUserIds().size() : 0);
        try {
            AiScriptResponseDto response = restTemplate.postForObject(url, requestDto, AiScriptResponseDto.class);
            if (response == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI 스크립트 생성 서버 응답이 null입니다.");
            }
            log.info("[AI Client] Received script generation response from AI server: scriptCount={}",
                    response.getScripts() != null ? response.getScripts().size() : 0);
            return response;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("[AI Client] AI 스크립트 생성 서버 연동 실패: url={}, error={}", url, e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "AI 스크립트 생성 서버 연동 실패: " + e.getMessage());
        }
    }

    public TtsResponseDto requestTtsGeneration(Object scriptPayload) {
        String url = aiServerUrl + "/briefings";
        try {
            String payloadJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(scriptPayload);
            log.info("[AI Client] Sending HTTP POST request for TTS generation: url={}\nRequest Body:\n{}", url, payloadJson);
        } catch (Exception e) {
            log.info("[AI Client] Sending HTTP POST request for TTS generation: url={}, payload={}", url, scriptPayload);
        }
        try {
            TtsResponseDto response = restTemplate.postForObject(url, scriptPayload, TtsResponseDto.class);
            if (response == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI TTS 서버 응답이 null입니다.");
            }
            log.info("[AI Client] Received TTS response from AI server: audioUrl={}", response.getAudioUrl());
            return response;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("[AI Client] AI TTS 서버 연동 실패: url={}, error={}", url, e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "AI TTS 서버 연동 실패: " + e.getMessage());
        }
    }
}
