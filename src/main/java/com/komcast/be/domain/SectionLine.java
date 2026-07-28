package com.komcast.be.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "section_lines")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SectionLine {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @Column(name = "line_order", nullable = false)
    private Integer lineOrder;

    @Column(name = "talker", nullable = false, length = 50)
    private String talker;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;
}
