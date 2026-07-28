package com.komcast.be.service;

import com.komcast.be.domain.Industry;
import com.komcast.be.domain.Stock;
import com.komcast.be.domain.User;
import com.komcast.be.domain.UserIndustry;
import com.komcast.be.domain.UserStock;
import com.komcast.be.dto.*;
import com.komcast.be.repository.IndustryRepository;
import com.komcast.be.repository.StockRepository;
import com.komcast.be.repository.UserIndustryRepository;
import com.komcast.be.repository.UserRepository;
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
    private final UserIndustryRepository userIndustryRepository;
    private final StockRepository stockRepository;
    private final IndustryRepository industryRepository;
    private final UserRepository userRepository;

    private static final List<StockResponseDto> DEFAULT_STOCKS = List.of(
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

    private static final List<IndustryResponseDto> DEFAULT_INDUSTRIES = List.of(
            new IndustryResponseDto("IND001", "반도체"),
            new IndustryResponseDto("IND002", "2차전지"),
            new IndustryResponseDto("IND003", "바이오/헬스케어"),
            new IndustryResponseDto("IND004", "금융"),
            new IndustryResponseDto("IND005", "AI/빅테크"),
            new IndustryResponseDto("IND006", "자동차"),
            new IndustryResponseDto("IND007", "엔터테인먼트"),
            new IndustryResponseDto("IND008", "게임"),
            new IndustryResponseDto("IND009", "화학"),
            new IndustryResponseDto("IND010", "건설/부동산"),
            new IndustryResponseDto("IND011", "에너지"),
            new IndustryResponseDto("IND012", "소비재"),
            new IndustryResponseDto("IND013", "통신"),
            new IndustryResponseDto("IND014", "방산")
    );

    @Transactional
    public List<StockResponseDto> getAllStocks() {
        List<Stock> dbStocks = stockRepository.findAll();
        if (dbStocks.isEmpty()) {
            for (StockResponseDto dto : DEFAULT_STOCKS) {
                stockRepository.save(Stock.builder()
                        .stockCode(dto.getCode())
                        .corpName(dto.getName())
                        .corpCode("CORP_" + dto.getCode())
                        .isKospi200(true)
                        .build());
            }
            dbStocks = stockRepository.findAll();
        }

        return dbStocks.stream()
                .map(s -> new StockResponseDto(s.getCorpName(), s.getStockCode(), 70000, 0.0))
                .collect(Collectors.toList());
    }

    @Transactional
    public List<IndustryResponseDto> getAllIndustries() {
        List<Industry> dbIndustries = industryRepository.findAll();
        if (dbIndustries.isEmpty()) {
            for (IndustryResponseDto dto : DEFAULT_INDUSTRIES) {
                industryRepository.save(Industry.builder()
                        .industryCode(dto.getCode())
                        .industryName(dto.getName())
                        .build());
            }
            dbIndustries = industryRepository.findAll();
        }

        return dbIndustries.stream()
                .map(i -> IndustryResponseDto.builder()
                        .code(i.getIndustryCode())
                        .name(i.getIndustryName())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public List<StockResponseDto> getMyStocks(Object userId) {
        User user = getOrCreateUser(userId);
        List<UserStock> userStocks = userStockRepository.findByUserId(user.getId());

        if (userStocks.isEmpty()) {
            return List.of();
        }

        Set<String> codeOrNames = userStocks.stream()
                .map(UserStock::getStockCode)
                .collect(Collectors.toSet());

        List<StockResponseDto> allStocks = getAllStocks();

        return allStocks.stream()
                .filter(s -> codeOrNames.contains(s.getCode()) || codeOrNames.contains(s.getName()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void registerMyStock(Object userId, StockRegisterRequestDto dto) {
        User user = getOrCreateUser(userId);
        if (!userStockRepository.existsByUserIdAndStockCode(user.getId(), dto.getCode())) {
            userStockRepository.save(UserStock.builder()
                    .user(user)
                    .stockCode(dto.getCode())
                    .type(dto.getType() != null ? dto.getType() : "PORTFOLIO")
                    .build());
        }
    }

    @Transactional
    public void registerMyStocksBatch(Object userId, StockBatchRegisterRequestDto dto) {
        User user = getOrCreateUser(userId);
        if (dto.getCodes() != null && !dto.getCodes().isEmpty()) {
            String type = dto.getType() != null ? dto.getType() : "PORTFOLIO";
            Set<String> uniqueCodes = new LinkedHashSet<>(dto.getCodes());
            for (String code : uniqueCodes) {
                if (!userStockRepository.existsByUserIdAndStockCode(user.getId(), code)) {
                    userStockRepository.save(UserStock.builder()
                            .user(user)
                            .stockCode(code)
                            .type(type)
                            .build());
                }
            }
        }
    }

    @Transactional
    public void deleteMyStock(Object userId, String code) {
        User user = getOrCreateUser(userId);
        userStockRepository.deleteByUserIdAndStockCode(user.getId(), code);
    }

    @Transactional
    public List<IndustryResponseDto> getMyIndustries(Object userId) {
        User user = getOrCreateUser(userId);
        List<IndustryResponseDto> allIndustries = getAllIndustries();
        Map<String, String> masterCodeToName = allIndustries.stream()
                .collect(Collectors.toMap(IndustryResponseDto::getCode, IndustryResponseDto::getName, (a, b) -> a));

        return userIndustryRepository.findByUserId(user.getId())
                .stream().map(ui -> {
                    String code = ui.getIndustryCode();
                    String name = ui.getIndustryName() != null ? ui.getIndustryName() : masterCodeToName.getOrDefault(code, code);
                    return IndustryResponseDto.builder()
                            .code(code)
                            .name(name)
                            .build();
                }).collect(Collectors.toList());
    }

    @Transactional
    public void registerMyIndustry(Object userId, IndustryRegisterRequestDto dto) {
        User user = getOrCreateUser(userId);
        List<IndustryResponseDto> allIndustries = getAllIndustries();
        Map<String, String> masterCodeToName = allIndustries.stream()
                .collect(Collectors.toMap(IndustryResponseDto::getCode, IndustryResponseDto::getName, (a, b) -> a));
        Map<String, String> masterNameToCode = allIndustries.stream()
                .collect(Collectors.toMap(IndustryResponseDto::getName, IndustryResponseDto::getCode, (a, b) -> a));

        String val = dto.getCode();
        String code = val;
        String name = val;
        if (masterCodeToName.containsKey(val)) {
            code = val;
            name = masterCodeToName.get(val);
        } else if (masterNameToCode.containsKey(val)) {
            code = masterNameToCode.get(val);
            name = val;
        }

        if (!userIndustryRepository.existsByUserIdAndIndustryCode(user.getId(), code)) {
            userIndustryRepository.save(UserIndustry.builder()
                    .user(user)
                    .industryCode(code)
                    .industryName(name)
                    .build());
        }
    }

    @Transactional
    public void registerMyIndustriesBatch(Object userId, IndustryBatchRegisterRequestDto dto) {
        User user = getOrCreateUser(userId);
        if (dto.getCodes() != null && !dto.getCodes().isEmpty()) {
            List<IndustryResponseDto> allIndustries = getAllIndustries();
            Map<String, String> masterCodeToName = allIndustries.stream()
                    .collect(Collectors.toMap(IndustryResponseDto::getCode, IndustryResponseDto::getName, (a, b) -> a));
            Map<String, String> masterNameToCode = allIndustries.stream()
                    .collect(Collectors.toMap(IndustryResponseDto::getName, IndustryResponseDto::getCode, (a, b) -> a));

            Set<String> uniqueCodes = new LinkedHashSet<>(dto.getCodes());
            for (String val : uniqueCodes) {
                String code = val;
                String name = val;
                if (masterCodeToName.containsKey(val)) {
                    code = val;
                    name = masterCodeToName.get(val);
                } else if (masterNameToCode.containsKey(val)) {
                    code = masterNameToCode.get(val);
                    name = val;
                }

                if (!userIndustryRepository.existsByUserIdAndIndustryCode(user.getId(), code)) {
                    userIndustryRepository.save(UserIndustry.builder()
                            .user(user)
                            .industryCode(code)
                            .industryName(name)
                            .build());
                }
            }
        }
    }

    @Transactional
    public void deleteMyIndustry(Object userId, String code) {
        User user = getOrCreateUser(userId);
        userIndustryRepository.deleteByUserIdAndIndustryCode(user.getId(), code);
    }

    @Transactional
    public User getOrCreateUser(Object userIdObj) {
        return userRepository.findAll().stream().findFirst()
                .orElseGet(() -> userRepository.save(User.builder()
                        .nickname("민준")
                        .plan("FREE")
                        .build())
                );
    }
}
