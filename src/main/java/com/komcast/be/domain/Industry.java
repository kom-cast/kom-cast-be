package com.komcast.be.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "industries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Industry extends BaseTimeEntity {

    @Id
    @Column(name = "industry_code", nullable = false)
    private String industryCode;

    @Column(name = "industry_name", nullable = false, unique = true)
    private String industryName;
}
