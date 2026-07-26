package com.komcast.be;

import com.komcast.be.dto.StockRegisterRequestDto;
import com.komcast.be.dto.StockResponseDto;
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
class StockServiceTest {

    @Autowired
    private StockService stockService;

    @Test
    @DisplayName("등록된 종목이 없는 경우 빈 배열을 반환해야 함")
    void getMyStocks_returnsEmptyList_whenNoStocksRegistered() {
        Long userId = 1L;

        List<StockResponseDto> myStocks = stockService.getMyStocks(userId);

        assertThat(myStocks).isEmpty();
    }

    @Test
    @DisplayName("종목 추가 후 삭제 시 정상적으로 DB에서 삭제되어야 함")
    void registerAndDeleteMyStock() {
        Long userId = 1L;

        // 1. 추가
        stockService.registerMyStock(userId, StockRegisterRequestDto.builder()
                .code("005930")
                .type("PORTFOLIO")
                .build());

        List<StockResponseDto> stocksAfterRegister = stockService.getMyStocks(userId);
        assertThat(stocksAfterRegister).hasSize(1);
        assertThat(stocksAfterRegister.get(0).getCode()).isEqualTo("005930");

        // 2. 삭제
        stockService.deleteMyStock(userId, "005930");

        List<StockResponseDto> stocksAfterDelete = stockService.getMyStocks(userId);
        assertThat(stocksAfterDelete).isEmpty();
    }
}
