package com.komcast.be.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiScriptResponseDto {

    private List<GeneratedScriptItem> scripts;
    private List<Object> failures;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GeneratedScriptItem {
        @JsonProperty("script_id")
        private String scriptId;

        @JsonProperty("user_id")
        private String userId;

        private Boolean reused;
    }
}
