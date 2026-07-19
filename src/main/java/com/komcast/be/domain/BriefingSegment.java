package com.komcast.be.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "briefing_segments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class BriefingSegment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "briefing_id", nullable = false)
    private Briefing briefing;

    @Column(name = "fraction", nullable = false)
    private Double fraction;

    @Column(name = "stock_name", nullable = false, length = 50)
    private String stockName;

    @Column(name = "text", nullable = false, columnDefinition = "TEXT")
    private String text;

    public void assignBriefing(Briefing briefing) {
        this.briefing = briefing;
    }
}
