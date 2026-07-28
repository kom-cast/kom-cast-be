package com.komcast.be.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "sections")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "section_type", nullable = false, columnDefinition = "varchar(30)")
    private SectionType sectionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, columnDefinition = "varchar(30)")
    private SectionTargetType targetType;

    @Column(name = "stock_code")
    private String stockCode;

    @Column(name = "industry_code")
    private String industryCode;

    @Column(name = "period_start", nullable = false)
    private ZonedDateTime periodStart;

    @Column(name = "period_end", nullable = false)
    private ZonedDateTime periodEnd;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;
}
