package com.komcast.be;

import com.komcast.be.domain.Industry;
import com.komcast.be.dto.IndustryBatchRegisterRequestDto;
import com.komcast.be.dto.IndustryRegisterRequestDto;
import com.komcast.be.dto.IndustryResponseDto;
import com.komcast.be.dto.StockBatchRegisterRequestDto;
import com.komcast.be.repository.IndustryRepository;
import com.komcast.be.service.PreferenceService;
import com.komcast.be.service.StockService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class Issue10ApiTest {

    @Autowired
    private StockService stockService;

    @Autowired
    private IndustryRepository industryRepository;

    @Autowired
    private PreferenceService preferenceService;

    @Test
    @DisplayName("관심 산업 등록, 중복 방지 및 삭제 테스트")
    void myIndustrySingleCrudTest() {
        Long userId = 1L;

        industryRepository.save(Industry.builder()
                .industryCode("IND001")
                .industryName("반도체")
                .build());

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

        industryRepository.save(Industry.builder()
                .industryCode("IND001")
                .industryName("반도체")
                .build());

        // 1. 종목 중복 포함 배치 등록
        stockService.registerMyStocksBatch(userId, StockBatchRegisterRequestDto.builder()
                .codes(List.of("005930", "005930", "000660"))
                .type("PORTFOLIO")
                .build());

        assertThat(stockService.getMyStocks(userId)).hasSize(2);

        // 2. 관심 산업 중복 포함 배치 등록
        stockService.registerMyIndustriesBatch(userId, IndustryBatchRegisterRequestDto.builder()
                .codes(List.of("IND001", "IND001", "IND002"))
                .build());

        assertThat(stockService.getMyIndustries(userId)).hasSize(2);
    }
}
