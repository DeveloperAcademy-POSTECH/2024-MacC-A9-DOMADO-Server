package com.onemorethink.domadosever.domain.user.entity;

import com.onemorethink.domadosever.domain.coupon.entity.Coupon;
import com.onemorethink.domadosever.domain.coupon.entity.Stamp;
import com.onemorethink.domadosever.domain.payment.entity.paymentMethod.PaymentMethod;
import com.onemorethink.domadosever.domain.rental.entity.Rental;
import com.onemorethink.domadosever.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
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

    @Column(nullable = false)
    private boolean hasRegisteredPayments = false;

    @OneToMany(mappedBy = "user")
    private Set<PaymentMethod> paymentMethods = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private List<Stamp> stamps = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Coupon> coupons = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

}
