package com.onemorethink.domadosever.global.apns.entity;

import com.onemorethink.domadosever.domain.user.entity.User;
import com.onemorethink.domadosever.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "device_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeviceToken extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 64)
    private String token;

    @Column(nullable = false)
    private boolean active = true;

    @Builder
    public DeviceToken(User user, String token) {
        this.user = user;
        this.token = token;
    }

    public void deactivate() {
        this.active = false;
    }
}
