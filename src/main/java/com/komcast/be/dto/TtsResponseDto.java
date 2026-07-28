package com.komcast.be.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class TtsResponseDto {

    @JsonProperty("audioUrl")
    private String audioUrl;

    @JsonProperty("durationSec")
    private Double durationSec;

    @JsonProperty("durationSeconds")
    public Double getDurationSeconds() {
        return durationSec;
    }

    private List<TtsSegmentItem> segments;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TtsSegmentItem {
        private String speaker;
        private TtsTargetItem target;
        private String text;

        @JsonProperty("startSec")
        private Double startSec;

        private Object words;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TtsTargetItem {
        private String type; // STOCK, INDUSTRY, USER

        @JsonProperty("stock_code")
        private String stockCode;

        @JsonProperty("industry_code")
        private String industryCode;

        @JsonIgnore
        public String getTargetCode() {
            if (stockCode != null) return stockCode;
            if (industryCode != null) return industryCode;
            return null;
        }
    }
}
