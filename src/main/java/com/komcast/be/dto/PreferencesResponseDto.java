package com.komcast.be.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreferencesResponseDto {
    private String nickname;
    private List<String> portfolio;
    private List<String> interests;
    private List<IndustryResponseDto> industries;
    private List<String> includeKeywords;
    private List<String> excludeKeywords;
    private String freeText;
    private String briefingDuration;
    private String voice;
    private Boolean notifyBriefing;
    private Boolean notifyPriceAlert;
    private Boolean notifyMarketing;
}
