package com.komcast.be.service;

import com.komcast.be.domain.User;
import com.komcast.be.domain.UserStock;
import com.komcast.be.dto.StockRegisterRequestDto;
import com.komcast.be.dto.StockResponseDto;
import com.komcast.be.repository.UserStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockService {

    private final UserStockRepository userStockRepository;
    private final PreferenceService preferenceService;

    private static final List<StockResponseDto> MASTER_STOCKS = List.of(
            new StockResponseDto("삼성전자", "005930", 73400, 1.2),
            new StockResponseDto("SK하이닉스", "000660", 189000, 3.4),
            new StockResponseDto("NAVER", "035420", 212500, -0.8),
            new StockResponseDto("LG화학", "051910", 428000, 0.5),
            new StockResponseDto("삼성SDI", "006400", 397000, -1.2),
            new StockResponseDto("카카오", "035720", 51300, 2.1),
            new StockResponseDto("삼성바이오로직스", "207940", 892000, 0.9),
            new StockResponseDto("LG에너지솔루션", "373220", 410000, 0.3),
            new StockResponseDto("현대차", "005380", 235000, -0.4),
            new StockResponseDto("기아", "000270", 98700, 1.8),
            new StockResponseDto("POSCO홀딩스", "005490", 412000, 2.5),
            new StockResponseDto("KB금융", "105560", 78900, 0.6)
    );

    private static final List<String> MASTER_SECTORS = List.of(
            "반도체", "2차전지", "바이오/헬스케어", "금융", "AI/빅테크",
            "자동차", "엔터테인먼트", "게임", "화학", "건설/부동산", "에너지", "소비재", "통신", "방산"
    );

    public List<StockResponseDto> getAllStocks() {
        return MASTER_STOCKS;
    }

    public List<String> getAllSectors() {
        return MASTER_SECTORS;
    }

    public List<StockResponseDto> getMyStocks(Long userId) {
        User user = preferenceService.getOrCreateUser(userId);
        List<UserStock> userStocks = userStockRepository.findByUserId(user.getId());

        if (userStocks.isEmpty()) {
            // 기본 종목이 없는 경우 샘플 등록
            return List.of(
                    new StockResponseDto("삼성전자", "005930", 73400, 1.2),
                    new StockResponseDto("SK하이닉스", "000660", 189000, 3.4)
            );
        }

        Set<String> codeOrNames = userStocks.stream()
                .map(UserStock::getStockCode)
                .collect(Collectors.toSet());

        return MASTER_STOCKS.stream()
                .filter(s -> codeOrNames.contains(s.getCode()) || codeOrNames.contains(s.getName()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void registerMyStock(Long userId, StockRegisterRequestDto dto) {
        User user = preferenceService.getOrCreateUser(userId);
        userStockRepository.save(UserStock.builder()
                .user(user)
                .stockCode(dto.getCode())
                .type(dto.getType() != null ? dto.getType() : "PORTFOLIO")
                .build());
    }

    @Transactional
    public void deleteMyStock(Long userId, String code) {
        User user = preferenceService.getOrCreateUser(userId);
        userStockRepository.deleteByUserIdAndStockCode(user.getId(), code);
    }
}
