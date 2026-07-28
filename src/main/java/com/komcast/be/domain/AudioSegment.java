package com.komcast.be.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "audio_segments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AudioSegment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audio_id", nullable = false)
    private Audio audio;

    @Column(name = "segment_order", nullable = false)
    private Integer segmentOrder;

    @Column(name = "speaker", nullable = false, length = 50)
    private String speaker;

    @Column(name = "stock_code", length = 20)
    private String stockCode;

    @Column(name = "text", nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(name = "start_sec", nullable = false)
    private Double startSec;

    @Column(name = "words", columnDefinition = "TEXT")
    private String words;
}
