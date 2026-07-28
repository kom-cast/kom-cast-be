package com.komcast.be.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

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
