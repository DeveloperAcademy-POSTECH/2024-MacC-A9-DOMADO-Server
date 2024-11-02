package com.onemorethink.domadosever.domain.user.entity;

import com.onemorethink.domadosever.domain.coupon.entity.Coupon;
import com.onemorethink.domadosever.domain.coupon.entity.Stamp;
import com.onemorethink.domadosever.domain.payment.entity.PaymentMethod;
import com.onemorethink.domadosever.domain.rental.entity.Rental;
import com.onemorethink.domadosever.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    private Integer penaltyCount = 0;

    @OneToOne
    @JoinColumn(name = "current_rental_id")
    private Rental currentRental;

    @OneToMany(mappedBy = "user")
    private List<Rental> rentalHistory = new ArrayList<>();

    @Column(nullable = false)
    private boolean hasRegisteredPayments = false;

    @OneToMany(mappedBy = "user")
    private List<PaymentMethod> paymentMethods = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Stamp> stamps = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Coupon> coupons = new ArrayList<>();

}
