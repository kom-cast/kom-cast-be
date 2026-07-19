package com.komcast.be.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_stocks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "stock_code", nullable = false, length = 10)
    private String stockCode;

    @Column(name = "type", nullable = false, length = 20)
    @Builder.Default
    private String type = "PORTFOLIO"; // PORTFOLIO, INTEREST
}
