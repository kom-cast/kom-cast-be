package com.komcast.be.service;

import com.komcast.be.domain.*;
import com.komcast.be.dto.*;
import com.komcast.be.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PreferenceService {

    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final UserStockRepository userStockRepository;
    private final UserIndustryRepository userIndustryRepository;
    private final UserKeywordRepository userKeywordRepository;
    private final StockService stockService;

    public PreferencesResponseDto getPreferences(Long userId) {
        User user = getOrCreateUser(userId);
        UserPreference pref = getOrCreatePreference(user);

        List<String> portfolio = userStockRepository.findByUserIdAndType(user.getId(), "PORTFOLIO")
                .stream().map(UserStock::getStockCode).collect(Collectors.toList());
        List<String> interests = userStockRepository.findByUserIdAndType(user.getId(), "INTEREST")
                .stream().map(UserStock::getStockCode).collect(Collectors.toList());

        Map<String, String> masterCodeToName = stockService.getAllIndustries().stream()
                .collect(Collectors.toMap(IndustryResponseDto::getCode, IndustryResponseDto::getName, (a, b) -> a));

        List<IndustryResponseDto> industries = userIndustryRepository.findByUserId(user.getId())
                .stream().map(ui -> {
                    String code = ui.getIndustryCode();
                    String name = ui.getIndustryName() != null ? ui.getIndustryName() : masterCodeToName.getOrDefault(code, code);
                    return IndustryResponseDto.builder()
                            .code(code)
                            .name(name)
                            .build();
                }).collect(Collectors.toList());

        List<String> includeKeywords = userKeywordRepository.findByUserIdAndType(user.getId(), "INCLUDE")
                .stream().map(UserKeyword::getKeyword).collect(Collectors.toList());
        List<String> excludeKeywords = userKeywordRepository.findByUserIdAndType(user.getId(), "EXCLUDE")
                .stream().map(UserKeyword::getKeyword).collect(Collectors.toList());

        return PreferencesResponseDto.builder()
                .nickname(user.getNickname())
                .portfolio(portfolio)
                .interests(interests)
                .industries(industries)
                .includeKeywords(includeKeywords)
                .excludeKeywords(excludeKeywords)
                .freeText(pref.getFreeText())
                .briefingDuration(String.valueOf(pref.getBriefingDuration()))
                .voice(pref.getVoice())
                .notifyBriefing(pref.getNotifyBriefing())
                .notifyPriceAlert(pref.getNotifyPriceAlert())
                .notifyMarketing(pref.getNotifyMarketing())
                .build();
    }

    @Transactional
    public void updatePreferences(Long userId, PreferencesUpdateRequestDto dto) {
        User user = getOrCreateUser(userId);
        if (dto.getNickname() != null) {
            user.updateNickname(dto.getNickname());
        }

        UserPreference pref = getOrCreatePreference(user);
        if (dto.getVoice() != null) pref.updateVoice(dto.getVoice());
        if (dto.getBriefingDuration() != null) {
            try {
                pref.updateBriefingDuration(Integer.parseInt(dto.getBriefingDuration()));
            } catch (NumberFormatException ignored) {}
        }
        pref.updateNotifications(dto.getNotifyBriefing(), dto.getNotifyPriceAlert(), dto.getNotifyMarketing());

        if (dto.getPortfolio() != null) {
            userStockRepository.deleteByUserId(user.getId());
            for (String stockCode : dto.getPortfolio()) {
                userStockRepository.save(UserStock.builder()
                        .user(user)
                        .stockCode(stockCode)
                        .type("PORTFOLIO")
                        .build());
            }
        }

        if (dto.getIndustries() != null) {
            userIndustryRepository.deleteByUserId(user.getId());
            Map<String, String> masterCodeToName = stockService.getAllIndustries().stream()
                    .collect(Collectors.toMap(IndustryResponseDto::getCode, IndustryResponseDto::getName, (a, b) -> a));
            Map<String, String> masterNameToCode = stockService.getAllIndustries().stream()
                    .collect(Collectors.toMap(IndustryResponseDto::getName, IndustryResponseDto::getCode, (a, b) -> a));

            for (String val : dto.getIndustries()) {
                String code = val;
                String name = val;
                if (masterCodeToName.containsKey(val)) {
                    code = val;
                    name = masterCodeToName.get(val);
                } else if (masterNameToCode.containsKey(val)) {
                    code = masterNameToCode.get(val);
                    name = val;
                }
                userIndustryRepository.save(UserIndustry.builder()
                        .user(user)
                        .industryCode(code)
                        .industryName(name)
                        .build());
            }
        }

        if (dto.getIncludeKeywords() != null || dto.getExcludeKeywords() != null) {
            userKeywordRepository.deleteByUserId(user.getId());
            if (dto.getIncludeKeywords() != null) {
                for (String kw : dto.getIncludeKeywords()) {
                    userKeywordRepository.save(UserKeyword.builder()
                            .user(user)
                            .keyword(kw)
                            .type("INCLUDE")
                            .build());
                }
            }
            if (dto.getExcludeKeywords() != null) {
                for (String kw : dto.getExcludeKeywords()) {
                    userKeywordRepository.save(UserKeyword.builder()
                            .user(user)
                            .keyword(kw)
                            .type("EXCLUDE")
                            .build());
                }
            }
        }
    }

    @Transactional
    public void updateVoice(Long userId, String voice) {
        User user = getOrCreateUser(userId);
        UserPreference pref = getOrCreatePreference(user);
        pref.updateVoice(voice);
    }

    @Transactional
    public void updateDuration(Long userId, String duration) {
        User user = getOrCreateUser(userId);
        UserPreference pref = getOrCreatePreference(user);
        try {
            pref.updateBriefingDuration(Integer.parseInt(duration));
        } catch (NumberFormatException ignored) {}
    }

    public NotificationToggleRequestDto getNotificationSettings(Long userId) {
        User user = getOrCreateUser(userId);
        UserPreference pref = getOrCreatePreference(user);
        return NotificationToggleRequestDto.builder()
                .notifyBriefing(pref.getNotifyBriefing())
                .notifyPriceAlert(pref.getNotifyPriceAlert())
                .notifyMarketing(pref.getNotifyMarketing())
                .build();
    }

    @Transactional
    public void updateNotifications(Long userId, NotificationToggleRequestDto dto) {
        User user = getOrCreateUser(userId);
        UserPreference pref = getOrCreatePreference(user);
        pref.updateNotifications(dto.getNotifyBriefing(), dto.getNotifyPriceAlert(), dto.getNotifyMarketing());
    }

    @Transactional
    public User getOrCreateUser(Long userId) {
        return userRepository.findById(userId)
                .or(() -> userRepository.findAll().stream().findFirst())
                .orElseGet(() -> userRepository.save(User.builder()
                        .nickname("민준")
                        .plan("FREE")
                        .build())
                );
    }

    @Transactional
    public UserPreference getOrCreatePreference(User user) {
        return userPreferenceRepository.findByUserId(user.getId()).orElseGet(() ->
                userPreferenceRepository.save(UserPreference.builder()
                        .user(user)
                        .briefingDuration(10)
                        .voice("jieun")
                        .freeText("반도체 위주로 대본을 생성해주세요.")
                        .notifyBriefing(true)
                        .notifyPriceAlert(true)
                        .notifyMarketing(false)
                        .build())
        );
    }
}
