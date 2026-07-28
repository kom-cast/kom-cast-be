package com.komcast.be.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "stocks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Stock extends BaseTimeEntity {

    @Id
    @Column(name = "stock_code", nullable = false)
    private String stockCode;

    @Column(name = "corp_code", nullable = false, unique = true)
    private String corpCode;

    @Column(name = "corp_name", nullable = false)
    private String corpName;

    @Column(name = "industry_code")
    private String industryCode;

    @Column(name = "is_kospi200", nullable = false)
    private Boolean isKospi200;

    @Column(name = "dart_modify_date")
    private LocalDate dartModifyDate;
}
