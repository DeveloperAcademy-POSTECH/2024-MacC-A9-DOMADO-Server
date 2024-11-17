package com.onemorethink.domadosever.domain.payment.entity;

import com.onemorethink.domadosever.domain.coupon.entity.Coupon;
import com.onemorethink.domadosever.domain.payment.entity.paymentMethod.PaymentMethod;
import com.onemorethink.domadosever.domain.rental.entity.Rental;
import com.onemorethink.domadosever.domain.user.entity.User;
import com.onemorethink.domadosever.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payments")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_id", nullable = false)
    private Rental rental;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id", nullable = false)
    private PaymentMethod paymentMethod;

    // 최종 결제 금액
    @Column(nullable = false)
    private Integer amount;

    // 할인 적용 전 원래 금액
    @Column(nullable = false)
    private Integer originalAmount;

    // 할인 금액
    @Column(nullable = false)
    private Integer discountAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(unique = true)
    private  String transactionId; // 외부 결제 시스템의 거래 ID

    private  String failureReason; // 거래 실패 사유

    @OneToOne(mappedBy = "usedPayment")
    private Coupon usedCoupon;

}
