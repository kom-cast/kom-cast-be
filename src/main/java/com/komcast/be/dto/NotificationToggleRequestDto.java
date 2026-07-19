package com.komcast.be.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationToggleRequestDto {
    private Boolean notifyBriefing;
    private Boolean notifyPriceAlert;
    private Boolean notifyMarketing;
}
