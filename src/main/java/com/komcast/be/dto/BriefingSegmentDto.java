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
public class BriefingSegmentDto {

    private Double fraction;
    private String speaker;
    private BriefingTargetDto target;
    private String text;

    @JsonProperty("startSec")
    private Double startSec;

    private List<WordTimestampDto> words;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BriefingTargetDto {
        private String type; // STOCK, INDUSTRY, USER

        @JsonProperty("stock_id")
        private String stockId;

        @JsonProperty("stock_code")
        private String stockCode;

        @JsonProperty("industry_id")
        private String industryId;

        @JsonProperty("industry_code")
        private String industryCode;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WordTimestampDto {
        private String text;

        @JsonProperty("startSec")
        private Double startSec;

        @JsonProperty("endSec")
        private Double endSec;
    }
}
