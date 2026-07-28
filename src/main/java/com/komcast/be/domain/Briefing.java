package com.komcast.be.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "briefings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Briefing extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "headline", nullable = false, length = 255)
    private String headline;

    @Column(name = "audio_url", nullable = false, columnDefinition = "TEXT")
    private String audioUrl;

    @Column(name = "duration_seconds", nullable = false)
    private Integer durationSeconds;

    @OneToMany(mappedBy = "briefing", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BriefingSegment> segments = new ArrayList<>();

    public void addSegment(BriefingSegment segment) {
        segments.add(segment);
        segment.setBriefing(this);
    }
}
