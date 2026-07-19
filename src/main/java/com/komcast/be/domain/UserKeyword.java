package com.komcast.be.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_keywords")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "keyword", nullable = false, length = 50)
    private String keyword;

    @Column(name = "type", nullable = false, length = 20)
    private String type; // INCLUDE, EXCLUDE
}
