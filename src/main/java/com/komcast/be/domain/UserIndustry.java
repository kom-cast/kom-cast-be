package com.komcast.be.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_industries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserIndustry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "industry_code", nullable = false, length = 20)
    private String industryCode;

    @Column(name = "industry_name", length = 50)
    private String industryName;
}
