package com.komcast.be.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "user_preferences")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserPreference extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "briefing_duration", nullable = false)
    @Builder.Default
    private Integer briefingDuration = 10;

    @Column(name = "voice", nullable = false, length = 50)
    @Builder.Default
    private String voice = "jieun";

    @Column(name = "free_text", columnDefinition = "TEXT")
    private String freeText;

    @Column(name = "notify_briefing", nullable = false)
    @Builder.Default
    private Boolean notifyBriefing = true;

    @Column(name = "notify_price_alert", nullable = false)
    @Builder.Default
    private Boolean notifyPriceAlert = true;

    @Column(name = "notify_marketing", nullable = false)
    @Builder.Default
    private Boolean notifyMarketing = false;

    public void updateVoice(String voice) {
        this.voice = voice;
    }

    public void updateBriefingDuration(Integer briefingDuration) {
        this.briefingDuration = briefingDuration;
    }

    public void updateNotifications(Boolean notifyBriefing, Boolean notifyPriceAlert, Boolean notifyMarketing) {
        if (notifyBriefing != null) this.notifyBriefing = notifyBriefing;
        if (notifyPriceAlert != null) this.notifyPriceAlert = notifyPriceAlert;
        if (notifyMarketing != null) this.notifyMarketing = notifyMarketing;
    }
}
