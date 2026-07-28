package com.komcast.be.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchCompletionRequestDto {
    private String runDate;
    private String status;
    private Map<String, String> jobs;
}
