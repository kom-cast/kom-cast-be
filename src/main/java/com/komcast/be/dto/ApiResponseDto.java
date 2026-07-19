package com.komcast.be.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponseDto {
    private String status;
    private String message;

    public static ApiResponseDto success(String message) {
        return ApiResponseDto.builder()
                .status("SUCCESS")
                .message(message)
                .build();
    }
}
