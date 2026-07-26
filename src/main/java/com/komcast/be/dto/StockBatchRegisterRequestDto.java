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
public class StockBatchRegisterRequestDto {
    private List<String> codes;
    private String type;
}
