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
public class BriefingResponseDto {
    private Object id;
    private String date;
    private String headline;
    private String audioUrl;
    private Integer durationSeconds;
    private List<BriefingSegmentDto> segments;
}
