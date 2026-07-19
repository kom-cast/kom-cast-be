package com.komcast.be.service;

import com.komcast.be.domain.*;
import com.komcast.be.dto.*;
import com.komcast.be.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PreferenceService {

    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final UserStockRepository userStockRepository;
    private final UserSectorRepository userSectorRepository;
    private final UserKeywordRepository userKeywordRepository;

    public PreferencesResponseDto getPreferences(Long userId) {
        User user = getOrCreateUser(userId);
        UserPreference pref = getOrCreatePreference(user);

        List<String> portfolio = userStockRepository.findByUserIdAndType(user.getId(), "PORTFOLIO")
                .stream().map(UserStock::getStockCode).collect(Collectors.toList());
        List<String> interests = userStockRepository.findByUserIdAndType(user.getId(), "INTEREST")
                .stream().map(UserStock::getStockCode).collect(Collectors.toList());
        List<String> sectors = userSectorRepository.findByUserId(user.getId())
                .stream().map(UserSector::getSectorName).collect(Collectors.toList());
        List<String> includeKeywords = userKeywordRepository.findByUserIdAndType(user.getId(), "INCLUDE")
                .stream().map(UserKeyword::getKeyword).collect(Collectors.toList());
        List<String> excludeKeywords = userKeywordRepository.findByUserIdAndType(user.getId(), "EXCLUDE")
                .stream().map(UserKeyword::getKeyword).collect(Collectors.toList());

        return PreferencesResponseDto.builder()
                .nickname(user.getNickname())
                .portfolio(portfolio)
                .interests(interests)
                .sectors(sectors)
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

        if (dto.getSectors() != null) {
            userSectorRepository.deleteByUserId(user.getId());
            for (String sectorName : dto.getSectors()) {
                userSectorRepository.save(UserSector.builder()
                        .user(user)
                        .sectorName(sectorName)
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

    @Transactional
    public void updateNotifications(Long userId, NotificationToggleRequestDto dto) {
        User user = getOrCreateUser(userId);
        UserPreference pref = getOrCreatePreference(user);
        pref.updateNotifications(dto.getNotifyBriefing(), dto.getNotifyPriceAlert(), dto.getNotifyMarketing());
    }

    @Transactional
    public User getOrCreateUser(Long userId) {
        return userRepository.findById(userId).orElseGet(() ->
                userRepository.save(User.builder()
                        .id(userId)
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
