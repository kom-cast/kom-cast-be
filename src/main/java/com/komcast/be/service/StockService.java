package com.komcast.be.service;

import com.komcast.be.domain.Industry;
import com.komcast.be.domain.MarketPrice;
import com.komcast.be.domain.Stock;
import com.komcast.be.domain.User;
import com.komcast.be.domain.UserIndustry;
import com.komcast.be.domain.UserStock;
import com.komcast.be.dto.*;
import com.komcast.be.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final MarketPriceRepository marketPriceRepository;
    private final UserRepository userRepository;

    public Page<StockResponseDto> getAllStocks(String keyword, Pageable pageable) {
        Page<Stock> dbStocks;
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            dbStocks = stockRepository.findByCorpNameContainingOrStockCodeContaining(kw, kw, pageable);
        } else {
            dbStocks = stockRepository.findAll(pageable);
        }
        return dbStocks.map(this::mapToStockResponseDto);
    }

    public List<StockResponseDto> getAllStocks() {
        List<Stock> dbStocks = stockRepository.findAll();
        return dbStocks.stream()
                .map(this::mapToStockResponseDto)
                .collect(Collectors.toList());
    }

    public List<IndustryResponseDto> getAllIndustries() {
        List<Industry> dbIndustries = industryRepository.findAll();
        return dbIndustries.stream()
                .map(i -> IndustryResponseDto.builder()
                        .code(i.getIndustryCode())
                        .name(i.getIndustryName())
                        .build())
                .collect(Collectors.toList());
    }

    public List<StockResponseDto> getMyStocks(Object userId) {
        User user = getOrCreateUser(userId);
        List<UserStock> userStocks = userStockRepository.findByUserId(user.getId());

        if (userStocks.isEmpty()) {
            return List.of();
        }

        Map<String, String> codeToNameMap = stockRepository.findAll().stream()
                .collect(Collectors.toMap(Stock::getStockCode, Stock::getCorpName, (a, b) -> a));

        return userStocks.stream()
                .map(us -> {
                    String code = us.getStockCode();
                    String name = codeToNameMap.getOrDefault(code, code);
                    int price = 0;
                    double change = 0.0;

                    Optional<MarketPrice> latestPrice = marketPriceRepository.findTopByStockCodeOrderByTradedAtDesc(code);
                    if (latestPrice.isPresent()) {
                        price = latestPrice.get().getClosePrice() != null ? latestPrice.get().getClosePrice().intValue() : 0;
                        change = latestPrice.get().getChangeRate() != null ? latestPrice.get().getChangeRate().doubleValue() : 0.0;
                    }

                    return new StockResponseDto(name, code, price, change);
                })
                .collect(Collectors.toList());
    }

    private StockResponseDto mapToStockResponseDto(Stock s) {
        String code = s.getStockCode();
        String name = s.getCorpName();
        int price = 0;
        double change = 0.0;

        Optional<MarketPrice> latestPrice = marketPriceRepository.findTopByStockCodeOrderByTradedAtDesc(code);
        if (latestPrice.isPresent()) {
            price = latestPrice.get().getClosePrice() != null ? latestPrice.get().getClosePrice().intValue() : 0;
            change = latestPrice.get().getChangeRate() != null ? latestPrice.get().getChangeRate().doubleValue() : 0.0;
        }

        return new StockResponseDto(name, code, price, change);
    }

    @Transactional
    public void registerMyStock(Object userId, StockRegisterRequestDto dto) {
        User user = getOrCreateUser(userId);
        if (dto != null && dto.getCode() != null && !userStockRepository.existsByUserIdAndStockCode(user.getId(), dto.getCode())) {
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
        if (dto != null && dto.getCodes() != null && !dto.getCodes().isEmpty()) {
            String type = dto.getType() != null ? dto.getType() : "PORTFOLIO";
            Set<String> uniqueCodes = new LinkedHashSet<>(dto.getCodes());
            for (String code : uniqueCodes) {
                if (code != null && !userStockRepository.existsByUserIdAndStockCode(user.getId(), code)) {
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

    public List<IndustryResponseDto> getMyIndustries(Object userId) {
        User user = getOrCreateUser(userId);
        Map<String, String> masterCodeToName = industryRepository.findAll().stream()
                .collect(Collectors.toMap(Industry::getIndustryCode, Industry::getIndustryName, (a, b) -> a));

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
        if (dto != null && dto.getCode() != null) {
            String val = dto.getCode();
            Map<String, String> masterCodeToName = industryRepository.findAll().stream()
                    .collect(Collectors.toMap(Industry::getIndustryCode, Industry::getIndustryName, (a, b) -> a));
            Map<String, String> masterNameToCode = industryRepository.findAll().stream()
                    .collect(Collectors.toMap(Industry::getIndustryName, Industry::getIndustryCode, (a, b) -> a));

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

    @Transactional
    public void registerMyIndustriesBatch(Object userId, IndustryBatchRegisterRequestDto dto) {
        User user = getOrCreateUser(userId);
        if (dto != null && dto.getCodes() != null && !dto.getCodes().isEmpty()) {
            Map<String, String> masterCodeToName = industryRepository.findAll().stream()
                    .collect(Collectors.toMap(Industry::getIndustryCode, Industry::getIndustryName, (a, b) -> a));
            Map<String, String> masterNameToCode = industryRepository.findAll().stream()
                    .collect(Collectors.toMap(Industry::getIndustryName, Industry::getIndustryCode, (a, b) -> a));

            Set<String> uniqueCodes = new LinkedHashSet<>(dto.getCodes());
            for (String val : uniqueCodes) {
                if (val != null) {
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
    }

    @Transactional
    public void deleteMyIndustry(Object userId, String code) {
        User user = getOrCreateUser(userId);
        userIndustryRepository.deleteByUserIdAndIndustryCode(user.getId(), code);
    }

    @Transactional
    public User getOrCreateUser(Object userIdObj) {
        if (userIdObj != null) {
            try {
                UUID userId = userIdObj instanceof UUID ? (UUID) userIdObj : UUID.fromString(userIdObj.toString());
                return userRepository.findById(userId)
                        .orElseGet(() -> userRepository.save(User.builder()
                                .id(userId)
                                .nickname("민준")
                                .plan("FREE")
                                .build()));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return userRepository.findAll().stream().findFirst()
                .orElseGet(() -> userRepository.save(User.builder()
                        .nickname("민준")
                        .plan("FREE")
                        .build())
                );
    }
}
