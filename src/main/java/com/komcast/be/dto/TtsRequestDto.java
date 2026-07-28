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
public class TtsRequestDto {

    @JsonProperty("script_id")
    private String scriptId;

    private List<TtsSection> sections;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TtsSection {
        @JsonProperty("script_type")
        private String sectionType;

        private TtsTarget target;
        private List<TtsLine> lines;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TtsTarget {
        private String type; // STOCK, INDUSTRY, USER

        @JsonProperty("stock_code")
        private String stockCode;

        @JsonProperty("industry_code")
        private String industryCode;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TtsLine {
        private String speaker;
        private String text;
    }
}
