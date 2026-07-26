package com.komcast.be;

import com.komcast.be.dto.*;
import com.komcast.be.service.PreferenceService;
import com.komcast.be.service.StockService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class Issue10ApiTest {

    @Autowired
    private StockService stockService;

    @Autowired
    private PreferenceService preferenceService;

    @Test
    @DisplayName("관심 산업 등록, 중복 방지 및 삭제 테스트")
    void myIndustrySingleCrudTest() {
        Long userId = 1L;

        // 1. 등록 1회차
        stockService.registerMyIndustry(userId, IndustryRegisterRequestDto.builder()
                .code("IND001")
                .build());

        // 2. 동일 산업 등록 2회차 (중복 시도)
        stockService.registerMyIndustry(userId, IndustryRegisterRequestDto.builder()
                .code("IND001")
                .build());

        List<IndustryResponseDto> myIndustries = stockService.getMyIndustries(userId);
        assertThat(myIndustries).hasSize(1);
        assertThat(myIndustries.get(0).getCode()).isEqualTo("IND001");
        assertThat(myIndustries.get(0).getName()).isEqualTo("반도체");

        // 3. 삭제
        stockService.deleteMyIndustry(userId, "IND001");
        assertThat(stockService.getMyIndustries(userId)).isEmpty();
    }

    @Test
    @DisplayName("종목 및 관심 산업 배치 중복 방지 일괄 등록 테스트")
    void batchRegistrationDuplicatePreventionTest() {
        Long userId = 1L;

        // 1. 종목 중복 포함 배치 등록
        stockService.registerMyStocksBatch(userId, StockBatchRegisterRequestDto.builder()
                .codes(List.of("005930", "005930", "000660"))
                .type("PORTFOLIO")
                .build());

        List<StockResponseDto> myStocks = stockService.getMyStocks(userId);
        assertThat(myStocks).hasSize(2);

        // 2. 산업 중복 포함 배치 등록
        stockService.registerMyIndustriesBatch(userId, IndustryBatchRegisterRequestDto.builder()
                .codes(List.of("IND001", "IND001", "IND002"))
                .build());

        List<IndustryResponseDto> myIndustries = stockService.getMyIndustries(userId);
        assertThat(myIndustries).hasSize(2);
    }

    @Test
    @DisplayName("알림 설정 조회 및 수정 테스트")
    void notificationSettingsTest() {
        Long userId = 1L;

        // 1. 수정
        preferenceService.updateNotifications(userId, NotificationToggleRequestDto.builder()
                .notifyBriefing(true)
                .notifyPriceAlert(false)
                .notifyMarketing(true)
                .build());

        // 2. 조회
        NotificationToggleRequestDto settings = preferenceService.getNotificationSettings(userId);
        assertThat(settings.getNotifyBriefing()).isTrue();
        assertThat(settings.getNotifyPriceAlert()).isFalse();
        assertThat(settings.getNotifyMarketing()).isTrue();
    }
}
