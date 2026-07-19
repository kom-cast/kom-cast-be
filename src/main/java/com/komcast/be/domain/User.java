package com.komcast.be.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nickname", length = 50)
    private String nickname;

    @Column(name = "plan", nullable = false, length = 20)
    @Builder.Default
    private String plan = "FREE";

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updatePlan(String plan) {
        this.plan = plan;
    }
}
