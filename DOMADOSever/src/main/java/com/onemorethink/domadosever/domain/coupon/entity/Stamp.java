package com.onemorethink.domadosever.domain.coupon.entity;

import com.onemorethink.domadosever.domain.rental.entity.Rental;
import com.onemorethink.domadosever.domain.user.entity.User;
import com.onemorethink.domadosever.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "stamps")
@Getter
@Setter
@NoArgsConstructor
public class Stamp extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private boolean isUsed = false;

    @ManyToOne(fetch = FetchType.LAZY)
    private Coupon exchangedCoupon;

    @OneToOne(fetch = FetchType.LAZY)
    private Rental rental;

}
